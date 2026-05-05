"""Flask error handler registry.

This module exports :func:`register_error_handlers`, called by
:func:`app.create_app`. It registers handlers for the standard 4xx/5xx
HTTP status codes (400, 401, 403, 404, 405, 415, 422, 500), a generic
:class:`werkzeug.exceptions.HTTPException` fallback, and a catch-all
generic :class:`Exception` handler.

Response shape: JSON object with ``{"error": "<message>", "status": <int>}``.
This is the conventional Express-style error envelope. When the original
Node.js source is supplied, the shape MUST be adjusted 1:1 to whatever
the Node error middleware emitted (per AAP Rule 1 -- preserve every
public API contract).

Why a single registration function (not a Blueprint)?
----------------------------------------------------
Error handlers in Flask are registered against the application instance
itself (or, optionally, scoped to a Blueprint). Because every handler
defined here applies globally to every request the application serves,
they MUST be registered on the ``app`` directly, not on a Blueprint.
A function that takes ``app`` as its sole parameter is the simplest
shape that fits the application factory pattern (AAP Section 0.4.3) --
:func:`app.create_app` calls :func:`register_error_handlers(app)` once,
after Blueprint registration, and every error route is wired up.

Why both status-code handlers AND a class-based fallback?
---------------------------------------------------------
Flask resolves error handlers in two passes for HTTP exceptions:

1. **Most-specific status-code match** -- if an exception has a
   ``code`` attribute (every :class:`werkzeug.exceptions.HTTPException`
   subclass does, e.g. ``NotFound.code == 404``), Flask first looks
   for a handler registered with that exact integer code via
   ``@app.errorhandler(404)``.
2. **Class-hierarchy match** -- otherwise, Flask walks the exception's
   MRO looking for a handler registered against the class itself, e.g.
   ``@app.errorhandler(HTTPException)`` matches anything below it.

This module registers both. Specific codes (400, 404, etc.) are
matched first so each can produce a tailored log line and message.
``HTTPException`` catches every other 4xx/5xx not enumerated above
(409 Conflict, 410 Gone, 429 Too Many Requests, etc.), avoiding
the need to enumerate every Werkzeug exception class. ``Exception``
catches programmer errors and library bugs and converts them to a
500 response (see Phase 9 below for debug-mode re-raise behaviour).

Why JSON envelope and not HTML error pages?
-------------------------------------------
The user's prompt -- "rewrite this node.js server in python 3 using
flask, preserving all functionalities of the original project" --
implies an API server. Express applications most commonly return JSON
errors (e.g., ``app.use((err, req, res, next) => res.status(500).json({error: ...}))``).
JSON is therefore the safest scaffold default. When the Node source
is supplied, the response shape MUST be adjusted 1:1 to whatever the
Node middleware emits (which may include ``details``, ``code``, or
``type`` fields, or rendered HTML pages). The :func:`_json_error`
helper accepts ``**extra`` so additional fields can be merged into
the envelope without changing the call site of every handler.

Why per-handler logging at INFO (4xx) and EXCEPTION (5xx)?
----------------------------------------------------------
4xx errors are typically client-side mistakes (wrong URL, bad payload,
missing auth) and not noisy enough to warrant a stack trace. Logging
each at ``INFO`` produces one line per failed request, which mirrors
the access-log behaviour of the Express ``morgan`` middleware. 5xx
errors are server-side and need the full traceback for diagnosis;
``logger.exception(...)`` records the active exception's traceback at
``ERROR`` level, mirroring the typical Node ``winston`` / ``pino``
unhandled-error pattern (AAP Rule R9).

References
----------
* AAP Section 0.4.1.1 -- *Target Architecture (Tree)*: lists this file
  with the description "@errorhandler registrations for 4xx/5xx parity".
* AAP Section 0.4.3 -- *Design Pattern Applications*: prescribes an
  "error-handler registry" module registering ``@app.errorhandler``
  callbacks for HTTP status codes (400, 401, 403, 404, 405, 415, 422,
  500) and for application-defined exception classes.
* AAP Section 0.5.1.1 -- *Part A -- Definite Transformations*: marks
  this file as ``CREATE`` with the description "register_error_handlers(app)
  registering @app.errorhandler(400/401/403/404/405/415/422/500) and
  any custom exception classes -- preserves Node response shapes".
* AAP Section 0.5.2.1 -- *Import Statement Updates*: maps Express
  ``app.use((err, req, res, next) => ...)`` -> Flask
  ``@app.errorhandler(...)``.
* AAP Section 0.7.1 -- *Refactoring-Specific Rules*: Rule 1 (preserve
  public contract -- status codes & body shapes), Rule 4 (idiomatic
  Flask -- ``@app.errorhandler``), Rule 7 (synchronous default), Rule 8
  (snake_case identifiers), Rule 9 (Translate, do not optimize), Rule 10
  (one generation pass).
* AAP Section 0.7.2.2 -- *Public API Preservation*: validation errors
  produce 400/422 with ``{"error": ..., "details": [...]}`` shape;
  authentication failures produce 401/403 with appropriate
  ``WWW-Authenticate`` semantics.
* AAP Rule R7 -- *Error handling*: Express error-handling middleware
  (``(err, req, res, next) => ...``) becomes Flask
  ``@app.errorhandler(ExceptionClass)`` and ``@app.errorhandler(StatusCode)``
  registrations; unhandled exceptions are routed to a generic
  ``@app.errorhandler(Exception)``.
"""

from __future__ import annotations

import logging

# ``typing.Tuple`` is the form mandated by this module's external-imports
# schema (see AAP Section 0.5.2.1 and the file's external_imports
# specification). The built-in ``tuple`` is preferable in modern Python,
# but the schema is authoritative for inter-agent contract clarity, so
# ``Tuple`` is imported here and the ``UP035`` / ``UP006`` ruff hints
# are suppressed locally rather than globally.
from typing import Tuple  # noqa: UP035

from flask import Flask, jsonify, request
from werkzeug.exceptions import HTTPException

# ---------------------------------------------------------------------------
# Module-level logger.
#
# Per AAP Rule R9 ("No raw print for logs"), every module obtains its logger
# via :func:`logging.getLogger` with the module's ``__name__``. This routes
# all output through the dictConfig pipeline configured in
# :mod:`app.logging_config`, so 4xx / 5xx events produced here participate in
# the same handler chain as application-level log records and arrive at
# whatever destination (stdout, stderr, file, syslog) the configuration
# selects -- mirroring the Node winston / pino / morgan logging pattern.
# ---------------------------------------------------------------------------
logger = logging.getLogger(__name__)


# ---------------------------------------------------------------------------
# Private helper: build the consistent JSON envelope used by every handler.
# ---------------------------------------------------------------------------
def _json_error(
    status_code: int, message: str, **extra: object
) -> Tuple[object, int]:  # noqa: UP006
    """Build a JSON error response with a consistent shape.

    The envelope is intentionally minimal -- two keys, ``error`` and
    ``status`` -- so it can be extended via the ``**extra`` keyword
    arguments without breaking any caller. Validation handlers, for
    example, can pass ``details=[{"field": "email", ...}]`` to surface
    structured per-field error information without rewriting the helper.

    The trailing ``status_code`` integer is the second element of the
    returned tuple. Flask's view-return semantics treat a 2-tuple of
    ``(body, status)`` as "build a response with this body and this
    status code", so callers can ``return _json_error(...)`` directly.

    Parameters
    ----------
    status_code : int
        HTTP status code to return (e.g. 400, 404, 500). MUST be a
        valid HTTP status integer; values outside the 100-599 range
        will be rejected by Werkzeug when constructing the Response.
    message : str
        Human-readable error message that will appear under the
        ``"error"`` key of the JSON body.
    **extra : object
        Additional fields to merge into the response body. Keys
        passed here override neither ``"error"`` nor ``"status"``
        unless explicitly named -- in which case the explicit value
        wins (``payload.update(extra)`` is the last write).

    Returns
    -------
    tuple
        A 2-tuple ``(json_response, status_code)`` ready to be
        returned from a Flask view function or error handler.

    Notes
    -----
    The return type is annotated as ``Tuple[object, int]`` rather than
    the more specific ``Tuple[flask.Response, int]`` because
    :func:`flask.jsonify` returns a :class:`flask.Response` instance
    but the caller does not depend on that type -- only on the fact
    that Flask's view-return machinery will accept the tuple. Using
    ``object`` keeps the signature loose and avoids a tight coupling
    to a specific Flask Response class shape.
    """
    payload = {"error": message, "status": status_code}
    payload.update(extra)
    return jsonify(payload), status_code


# ---------------------------------------------------------------------------
# Public registration entry point.
# ---------------------------------------------------------------------------
def register_error_handlers(app: Flask) -> None:
    """Register HTTP and exception error handlers on the given Flask app.

    Called by :func:`app.create_app` AFTER configuration has been loaded
    and Blueprints have been registered. Each handler returns a JSON
    envelope (``{"error": ..., "status": ...}``) with the appropriate
    HTTP status code, mirroring the Express convention of
    ``res.status(N).json({...})`` from a centralised error middleware.

    Registered handlers
    -------------------
    * **400 Bad Request** (``handle_bad_request``) -- malformed request
      body, missing required parameters, JSON parse failures.
    * **401 Unauthorized** (``handle_unauthorized``) -- missing or
      invalid authentication credentials.
    * **403 Forbidden** (``handle_forbidden``) -- authenticated but
      not permitted to access the resource.
    * **404 Not Found** (``handle_not_found``) -- URL did not match
      any registered route, or a referenced resource does not exist.
    * **405 Method Not Allowed** (``handle_method_not_allowed``) --
      route exists but the HTTP method is not registered for it.
    * **415 Unsupported Media Type** (``handle_unsupported_media_type``)
      -- ``Content-Type`` is not one the endpoint accepts.
    * **422 Unprocessable Entity** (``handle_unprocessable_entity``) --
      well-formed but semantically invalid payload (typically used for
      schema validation failures).
    * **500 Internal Server Error** (``handle_internal_server_error``)
      -- explicit 500 raised by application code; the handler logs the
      full traceback via :meth:`logging.Logger.exception`.
    * **HTTPException fallback** (``handle_http_exception``) -- any
      other Werkzeug HTTP exception not enumerated above (409, 410, 429,
      etc.) is converted to the same JSON envelope.
    * **Generic Exception** (``handle_unexpected_exception``) -- any
      non-HTTP exception (programmer errors, library bugs, network
      failures) is logged with a full traceback and converted to a 500
      response. In ``DEBUG`` or ``PROPAGATE_EXCEPTIONS`` mode, the
      exception is re-raised so Werkzeug's interactive debugger engages.

    Idempotency
    -----------
    Calling this function more than once on the same app will register
    each handler more than once; Flask silently overwrites the previous
    registration, so the net effect is the same as a single call.
    Nevertheless, callers SHOULD invoke this function exactly once per
    Flask application instance, from :func:`app.create_app`.

    Thread-safety
    -------------
    This function is intended to be called once per Flask application
    instance from the main thread before any worker threads or request
    contexts exist. It is NOT designed for concurrent invocation.

    Parameters
    ----------
    app : flask.Flask
        The Flask application instance to register handlers on. MUST
        NOT be ``None``; passing ``None`` will surface an
        :class:`AttributeError` from the very first
        ``@app.errorhandler(...)`` decorator below.

    Returns
    -------
    None
        Side-effect-only: the supplied ``app`` is mutated in place via
        :meth:`flask.Flask.errorhandler` decorator registrations.
    """

    # ---------------------- 400 Bad Request --------------------------
    @app.errorhandler(400)
    def handle_bad_request(error):  # type: ignore[unused-ignore]
        """Return a JSON 400 response for malformed requests.

        The ``error`` argument is a :class:`werkzeug.exceptions.BadRequest`
        instance (when raised by Werkzeug's request parsing) or an
        :class:`werkzeug.exceptions.HTTPException` produced by
        :func:`flask.abort(400)`. Its ``description`` attribute carries
        the human-readable message; we fall back to a constant string
        for safety in case a non-Werkzeug exception is somehow routed
        here.
        """
        message = getattr(error, "description", "Bad Request")
        logger.info(
            "400 Bad Request: %s %s — %s",
            request.method,
            request.path,
            message,
        )
        return _json_error(400, message)

    # ---------------------- 401 Unauthorized -------------------------
    @app.errorhandler(401)
    def handle_unauthorized(error):  # type: ignore[unused-ignore]
        """Return a JSON 401 response for unauthenticated requests.

        Per AAP Section 0.7.2.2, ``WWW-Authenticate`` header semantics
        MUST match the Node original. Werkzeug's :class:`Unauthorized`
        exception sets the ``WWW-Authenticate`` header automatically
        when the exception is raised with a ``www_authenticate``
        argument; that behaviour is preserved here because the
        underlying exception's ``get_response()`` is not invoked --
        but if a future change requires emitting a default
        ``WWW-Authenticate`` header, it should be added to the
        :func:`_json_error` extras (e.g. via a wrapping ``make_response``).
        """
        message = getattr(error, "description", "Unauthorized")
        logger.info(
            "401 Unauthorized: %s %s — %s",
            request.method,
            request.path,
            message,
        )
        return _json_error(401, message)

    # ---------------------- 403 Forbidden ----------------------------
    @app.errorhandler(403)
    def handle_forbidden(error):  # type: ignore[unused-ignore]
        """Return a JSON 403 response for authenticated-but-disallowed requests."""
        message = getattr(error, "description", "Forbidden")
        logger.info(
            "403 Forbidden: %s %s — %s",
            request.method,
            request.path,
            message,
        )
        return _json_error(403, message)

    # ---------------------- 404 Not Found ----------------------------
    @app.errorhandler(404)
    def handle_not_found(error):  # type: ignore[unused-ignore]
        """Return a JSON 404 response for unknown URLs / missing resources.

        404 messages are intentionally less verbose than other 4xx
        log lines because high-traffic services often see a steady
        stream of probing requests against well-known paths
        (``/wp-admin``, ``/.env``, etc.); logging the description on
        each would just amplify the noise without adding signal.
        """
        message = getattr(error, "description", "Not Found")
        logger.info(
            "404 Not Found: %s %s",
            request.method,
            request.path,
        )
        return _json_error(404, message)

    # ---------------------- 405 Method Not Allowed -------------------
    @app.errorhandler(405)
    def handle_method_not_allowed(error):  # type: ignore[unused-ignore]
        """Return a JSON 405 response for the wrong HTTP method.

        Werkzeug's :class:`MethodNotAllowed` exception sets the
        ``Allow`` header automatically with the list of valid methods;
        that header is part of the response that Flask builds and is
        already preserved in the standard error path. The JSON body
        added here augments rather than replaces that behaviour.
        """
        message = getattr(error, "description", "Method Not Allowed")
        logger.info(
            "405 Method Not Allowed: %s %s",
            request.method,
            request.path,
        )
        return _json_error(405, message)

    # ---------------------- 415 Unsupported Media Type ---------------
    @app.errorhandler(415)
    def handle_unsupported_media_type(error):  # type: ignore[unused-ignore]
        """Return a JSON 415 response for unacceptable Content-Type."""
        message = getattr(error, "description", "Unsupported Media Type")
        logger.info(
            "415 Unsupported Media Type: %s %s — %s",
            request.method,
            request.path,
            message,
        )
        return _json_error(415, message)

    # ---------------------- 422 Unprocessable Entity -----------------
    @app.errorhandler(422)
    def handle_unprocessable_entity(error):  # type: ignore[unused-ignore]
        """Return a JSON 422 response for semantic validation failures.

        Per AAP Section 0.7.2.2, validation errors should produce
        either 400 or 422 with a JSON shape that includes a
        ``"details"`` array. This handler emits the bare envelope; the
        validation Blueprint / service that raises the 422 SHOULD pass
        the field-level details via ``flask.abort(422, description=...)``
        with a custom :class:`werkzeug.exceptions.HTTPException`
        subclass that overrides ``get_response()``, OR by directly
        returning ``_json_error(422, "Validation failed", details=[...])``
        from the view function. Until the Node source is supplied, the
        scaffold leaves that decision to the implementing agent.
        """
        message = getattr(error, "description", "Unprocessable Entity")
        logger.info(
            "422 Unprocessable Entity: %s %s — %s",
            request.method,
            request.path,
            message,
        )
        return _json_error(422, message)

    # ---------------------- 500 Internal Server Error ----------------
    @app.errorhandler(500)
    def handle_internal_server_error(error):  # type: ignore[unused-ignore]
        """Return a JSON 500 response for explicit server errors.

        Uses :meth:`logging.Logger.exception` (NOT ``logger.error``) so
        the full traceback is captured at ``ERROR`` level. ``exception``
        is equivalent to ``error`` plus ``exc_info=True``, which causes
        the active exception's traceback to be appended to the log
        record. This is essential for diagnosing 500s in production.

        The message returned to the client is the constant string
        ``"Internal Server Error"`` -- NOT ``error.description`` --
        because echoing the description back to the client risks
        leaking internal state (stack frames, secret values that
        appeared in an exception message, etc.). Diagnosis happens via
        the log line, which never reaches the wire.
        """
        logger.exception(
            "500 Internal Server Error: %s %s",
            request.method,
            request.path,
        )
        return _json_error(500, "Internal Server Error")

    # ---------------------- HTTPException fallback ------------------
    @app.errorhandler(HTTPException)
    def handle_http_exception(error: HTTPException):  # type: ignore[unused-ignore]
        """Return a JSON response for any HTTP exception not handled above.

        Catches Werkzeug HTTP exceptions whose status codes are not
        explicitly enumerated in the AAP (409 Conflict, 410 Gone, 412
        Precondition Failed, 413 Payload Too Large, 429 Too Many
        Requests, 502, 503, etc.). Each is logged at ``INFO`` and
        converted to the same JSON envelope.

        ``error.code`` may legitimately be ``None`` for the abstract
        :class:`HTTPException` base class itself; the ``or 500``
        fallback ensures we never return a non-integer status. Equally,
        ``error.description`` may be ``None`` for some custom subclasses
        -- the chained ``or error.name or "HTTP Exception"`` ensures we
        always have a non-empty message string.
        """
        status_code = error.code or 500
        message = error.description or error.name or "HTTP Exception"
        logger.info(
            "%d %s: %s %s — %s",
            status_code,
            error.name,
            request.method,
            request.path,
            message,
        )
        return _json_error(status_code, message)

    # ---------------------- Generic Exception -----------------------
    @app.errorhandler(Exception)
    def handle_unexpected_exception(error: Exception):  # type: ignore[unused-ignore]
        """Convert any non-HTTP exception to a JSON 500.

        This is the catch-all that backs the "graceful degradation"
        contract: even if a programmer error, a third-party library
        bug, or a transient network failure raises an unexpected
        exception inside a view, the client receives a clean JSON 500
        response rather than an HTML stack trace page. The full
        traceback is captured in the server log via
        :meth:`logging.Logger.exception`.

        Debug-mode re-raise
        -------------------
        Without re-raising, Werkzeug's interactive debugger never
        engages: catching the exception here short-circuits Flask's
        debug-time exception propagation. To preserve the developer
        experience in local runs, we re-raise when ``app.config['DEBUG']``
        or ``app.config['PROPAGATE_EXCEPTIONS']`` is truthy.

        Why check both flags?
        --------------------
        ``DEBUG`` is the typical user-facing toggle (set via the
        ``FLASK_DEBUG`` env var or ``DevelopmentConfig``).
        ``PROPAGATE_EXCEPTIONS`` is the Flask-internal flag that
        controls whether the framework itself re-raises
        unhandled exceptions; some testing setups (notably
        ``pytest-flask``) flip it independently of ``DEBUG``. Honouring
        both means tests that expect to see the original exception
        propagate (rather than a JSON 500) continue to do so.

        Parameters
        ----------
        error : Exception
            The unhandled exception. The active traceback is preserved
            on the call stack and recorded by :meth:`logger.exception`.
        """
        logger.exception(
            "Unhandled exception during request: %s %s",
            request.method,
            request.path,
        )
        # Preserve the Werkzeug interactive debugger and pytest's
        # exception-bubbling behaviour. ``app.config.get`` is used
        # rather than the bracket form so a missing key returns the
        # safe default (``False``) instead of raising ``KeyError``.
        if app.config.get("DEBUG", False) or app.config.get("PROPAGATE_EXCEPTIONS", False):
            raise error
        return _json_error(500, "Internal Server Error")

    # ---------------------- Registration complete --------------------
    # A single ``DEBUG`` line per :func:`create_app` invocation provides
    # a useful breadcrumb during local development without producing
    # noise in production (where ``LOG_LEVEL`` is typically ``INFO``).
    logger.debug("Error handlers registered.")


# ---------------------------------------------------------------------------
# Public API surface.
#
# Only :func:`register_error_handlers` is part of this module's public
# contract. ``_json_error`` is a private helper (leading underscore plus
# explicit absence from ``__all__``) so that ``from app.errors import *``
# imports only the registration function, and IDEs / linters surface
# the helper as internal. Adding to this list MUST be a deliberate API
# decision, accompanied by tests.
# ---------------------------------------------------------------------------
__all__ = ["register_error_handlers"]
