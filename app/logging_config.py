"""Logging configuration for the Flask application.

This module exports :func:`configure_logging`, called by
:func:`app.create_app`. It applies a :func:`logging.config.dictConfig`
schema based on the application's ``LOG_LEVEL`` and ``LOG_FORMAT``
configuration values.

Two output formats are supported:

* ``plain`` -- human-readable single-line logs for development.
* ``json``  -- structured single-line JSON for production log aggregators.

When the original Node.js source's logger choice is identified
(``winston`` / ``pino`` / ``bunyan``), this schema is adjusted 1:1 to
emit byte-equivalent log lines so that downstream parsers continue to
work unchanged (per AAP Section 0.1.1.3 implicit requirement -- "Logging
output parity").

Why a custom JSON formatter (instead of ``python-json-logger``)?
---------------------------------------------------------------
Per AAP Section 0.6.1.2, ``python-json-logger`` is a *conditional*
dependency -- it is only added to ``requirements.txt`` when the Node
source uses a JSON-emitting logger. The scaffold delivered today must
be runnable with only the *definite-core* dependencies pinned in AAP
Section 0.6.1.1 (Flask 3.1.3 plus its transitives, ``python-dotenv``,
``gunicorn``). Implementing the JSON formatter using only the standard
library (``json`` + ``logging``) keeps the dependency surface minimal
while still producing parser-friendly output today.

Why ``disable_existing_loggers: False``?
----------------------------------------
The ``logging.config.dictConfig`` default is ``True``, which silently
*disables* every logger created BEFORE this function runs. That would
drop important early-bootstrap log lines (anything emitted by Flask,
Werkzeug, or the application's own modules between import time and
factory call time). Setting ``False`` preserves them.

Why explicit ``werkzeug`` logger configuration?
-----------------------------------------------
Without it, the Werkzeug development server prints duplicate request
log lines (one through the root logger, one through ``werkzeug``).
Setting ``werkzeug`` to ``WARNING`` keeps the development server quiet
during local runs; production deployments under ``gunicorn`` do not
trigger this logger at all.

Why ``propagate: False`` on the ``app`` logger?
-----------------------------------------------
Without it, log records emitted by application modules (which all sit
beneath the ``app.*`` dotted-name hierarchy) would propagate to the
root logger and be emitted twice. Setting ``propagate: False`` ensures
each record is handled by exactly one handler chain.

Why pre-build the schema in a private helper?
---------------------------------------------
Separating the dictionary construction from the ``dictConfig`` call
makes the schema testable: a unit test can call
:func:`_build_dict_config` and assert on its keys without mutating the
process-global logging state.

References
----------
* AAP Section 0.4.1.1 -- *Target Architecture (Tree)*: ``logging.dictConfig``
  setup mirroring Node.js logger output.
* AAP Section 0.4.3 -- *Design Pattern Applications*: structured logging
  via :func:`logging.config.dictConfig`.
* AAP Section 0.5.1.1 -- *Part A -- Definite Transformations*: marks this
  file as ``CREATE``.
* AAP Section 0.5.2.1 -- *Import Statement Updates*: maps Node ``winston``
  -> Python ``logging`` plus this module.
* AAP Section 0.6.1.2 -- *Conditional Dependencies*: ``python-json-logger``
  remains conditional; this module deliberately uses only stdlib.
* AAP Section 0.7.1 -- *Refactoring-Specific Rules*: Rule 1 (preserve
  public contract -- log output parity), Rule 6 (exact pins), Rule 9
  (no raw ``print`` for logs), Rule 10 (one generation pass).
"""

from __future__ import annotations

import json
import logging
import logging.config
import sys  # noqa: F401 -- referenced via the "ext://sys.stdout" dictConfig string
import time
from typing import Any, Dict  # noqa: UP035

from flask import Flask

# ---------------------------------------------------------------------------
# Module-level logger.
#
# Per AAP Rule R9 ("No raw print for logs"), every module obtains its logger
# via :func:`logging.getLogger` with the module's ``__name__``. Routing the
# logger through Python's hierarchical namespace (``app.logging_config``)
# means that :func:`configure_logging` can target the entire ``app.*``
# subtree by configuring the ``app`` logger -- which automatically applies
# to ``app.logging_config``, ``app.config``, ``app.extensions``, and every
# blueprint / service / model module added later.
#
# IMPORTANT: ``sys`` is imported above (rather than only referenced as a
# string in the schema) so that the symbol is in the module's globals when
# :func:`logging.config.dictConfig` resolves the ``ext://sys.stdout``
# reference. The ``ext://`` prefix tells dictConfig to look up the
# attribute by walking dotted names; the parent module (``sys``) must be
# importable when the schema is processed. Importing it eagerly here makes
# that resolution deterministic regardless of any other imports.
# ---------------------------------------------------------------------------
logger = logging.getLogger(__name__)


# ---------------------------------------------------------------------------
# JSON formatter -- stdlib-only implementation.
# ---------------------------------------------------------------------------


class JsonFormatter(logging.Formatter):
    """Minimal JSON log formatter using only the Python standard library.

    Emits one JSON object per :class:`logging.LogRecord`, with the
    canonical keys ``time`` (ISO 8601 UTC, millisecond precision),
    ``level`` (the standard level name -- ``"INFO"``, ``"DEBUG"``, etc.),
    ``logger`` (the dotted logger name, e.g. ``"app.blueprints.users"``),
    and ``message`` (the fully formatted message with %s/%d substitutions
    already applied).

    Any ``extra={...}`` keyword passed to a logging call (or any custom
    attribute set on the :class:`logging.LogRecord` in a filter) is
    rendered as an additional top-level JSON key. This mirrors the
    behaviour of ``winston`` (Node) emitting metadata fields and ``pino``
    accepting a "merging object" first argument.

    Parser-parity adjustments
    -------------------------
    When the Node source uses ``pino`` (whose default JSON keys are
    ``time``, ``level``, ``msg``, ``pid``, ``hostname``), the ``message``
    key emitted here can be renamed to ``msg`` (and ``pid`` / ``hostname``
    fields added) so that downstream parsers see byte-equivalent output.
    The current scaffold uses ``message`` (the more conventional name)
    because no Node source has been supplied to disambiguate.

    Encoding
    --------
    :func:`json.dumps` is invoked with ``default=str`` so that ``extra``
    values that are not natively JSON-serialisable (``datetime``,
    ``UUID``, ``pathlib.Path``, custom dataclasses, etc.) fall back to
    their ``str(...)`` representation rather than raising
    :class:`TypeError`. ``ensure_ascii=False`` preserves non-ASCII
    characters in messages (e.g., user-supplied content) without
    escaping them to ``\\uXXXX`` sequences -- modern log aggregators
    are UTF-8 native and the escaping just bloats payloads.

    Exceptions and stacks
    ---------------------
    When a :class:`logging.LogRecord` carries ``exc_info`` (set by any
    ``logger.exception(...)`` call or by ``logger.error(..., exc_info=True)``),
    the formatted traceback is added under the ``exception`` key. When
    ``stack_info`` is set (typically via ``logger.<level>(..., stack_info=True)``),
    the formatted stack is added under the ``stack`` key. Both are
    multi-line strings; downstream JSON consumers handle embedded
    newlines correctly because the entire payload is a single JSON
    string value.
    """

    # ``LogRecord`` instances carry a fixed set of standard attributes
    # populated by the logging framework itself. Anything OUTSIDE this
    # set is treated as user-supplied "extra" metadata (passed via the
    # ``extra={...}`` keyword to a logger call, or set on the record by
    # a custom :class:`logging.Filter` / :class:`logging.LoggerAdapter`).
    # Maintaining the set as a class-level constant means the lookup is
    # O(1) per attribute; using a frozenset would be marginally faster
    # but a regular set is sufficient and matches the Python stdlib
    # convention.
    #
    # The list mirrors :attr:`logging.LogRecord.__dict__` for Python 3.12
    # (the project's pinned interpreter, per ``.python-version``):
    # ``name``, ``msg``, ``args``, ``levelname``, ``levelno``,
    # ``pathname``, ``filename``, ``module``, ``exc_info``, ``exc_text``,
    # ``stack_info``, ``lineno``, ``funcName``, ``created``, ``msecs``,
    # ``relativeCreated``, ``thread``, ``threadName``, ``processName``,
    # ``process``. The synthetic ``message`` and ``asctime`` are added
    # by :meth:`logging.Formatter.format`, and ``taskName`` is the
    # async-task field added in Python 3.12.
    _STANDARD_ATTRS: set[str] = {
        "name",
        "msg",
        "args",
        "levelname",
        "levelno",
        "pathname",
        "filename",
        "module",
        "exc_info",
        "exc_text",
        "stack_info",
        "lineno",
        "funcName",
        "created",
        "msecs",
        "relativeCreated",
        "thread",
        "threadName",
        "processName",
        "process",
        "message",
        "asctime",
        "taskName",
    }

    def format(self, record: logging.LogRecord) -> str:
        """Render the supplied :class:`logging.LogRecord` as a single
        JSON object on one line.

        Parameters
        ----------
        record : logging.LogRecord
            The log record produced by the logging framework. Must not
            be ``None``.

        Returns
        -------
        str
            A single-line JSON-encoded string. Always terminates without
            a trailing newline (the :class:`logging.StreamHandler` adds
            the line terminator separately).

        Notes
        -----
        The implementation is robust against three classes of failure
        seen in the wild:

        * **Non-serialisable extras**: handled via ``default=str`` --
          objects without a JSON representation fall back to ``repr``.
        * **Late-bound message arguments**: handled by calling
          :meth:`logging.LogRecord.getMessage` which performs the %-style
          substitution. Calling ``record.msg`` directly would emit the
          unformatted template.
        * **Unicode safety**: ``ensure_ascii=False`` keeps non-ASCII
          characters intact; ``json.dumps`` always returns a ``str``,
          so the StreamHandler can encode it consistently.
        """
        # Build the canonical four-field payload first.
        #
        # ``time`` uses ISO 8601 in UTC with millisecond precision (the
        # precision used by ``winston``, ``pino``, and ``bunyan`` by
        # default). The construction is done manually rather than via
        # :meth:`logging.Formatter.formatTime` because that method
        # delegates to :func:`time.strftime`, which (unlike
        # :meth:`datetime.datetime.strftime`) does NOT support the
        # ``%f`` microsecond directive -- a literal ``%f`` would survive
        # in the output. The standard logging idiom is to compose the
        # date with :func:`time.strftime` and append ``%(msecs)03d``
        # separately; we replicate that here using ``record.created``
        # (Unix-epoch seconds, float) and ``record.msecs`` (the
        # millisecond fraction, also a float). ``time.gmtime`` (rather
        # than ``time.localtime``) ensures the timestamp is in UTC, so
        # operators reading logs in any time zone correctly interpret
        # the ``Z`` suffix.
        #
        # ``getMessage`` performs the %-style argument substitution that
        # the logging framework defers (e.g., ``logger.info("hi %s", name)``
        # is stored as ``msg="hi %s"`` and ``args=(name,)``; we want the
        # joined string).
        time_str = (
            time.strftime("%Y-%m-%dT%H:%M:%S", time.gmtime(record.created))
            + f".{int(record.msecs):03d}Z"
        )
        payload: Dict[str, Any] = {  # noqa: UP006
            "time": time_str,
            "level": record.levelname,
            "logger": record.name,
            "message": record.getMessage(),
        }

        # Merge any user-supplied ``extra={...}`` attributes. We exclude
        # the standard set above and any attribute starting with "_"
        # (to avoid leaking private/internal record helpers added by
        # custom filters or third-party logging plugins).
        for key, value in record.__dict__.items():
            if key in self._STANDARD_ATTRS or key.startswith("_"):
                continue
            payload[key] = value

        # Attach exception traceback (if the record carries one). The
        # parent class's ``formatException`` already returns a multi-line
        # string with the standard traceback layout; embedding it as a
        # single JSON string value preserves it in a JSON-safe way.
        if record.exc_info:
            payload["exception"] = self.formatException(record.exc_info)

        # Attach stack info (if the record carries one). This is set by
        # ``logger.<level>(..., stack_info=True)`` and is independent of
        # ``exc_info``.
        if record.stack_info:
            payload["stack"] = self.formatStack(record.stack_info)

        # Encode. ``default=str`` is the safety net for non-serialisable
        # values (datetime, UUID, Path, Decimal, custom classes). Without
        # it, a single rogue ``extra`` field would raise TypeError and
        # propagate up the logging stack -- which the framework catches
        # and prints to stderr, producing confusing operator-visible
        # noise. Falling back to ``str`` preserves observability at the
        # cost of slightly less faithful representations.
        return json.dumps(payload, default=str, ensure_ascii=False)


# ---------------------------------------------------------------------------
# dictConfig schema construction.
# ---------------------------------------------------------------------------


def _build_dict_config(level: str, output_format: str) -> Dict[str, Any]:  # noqa: UP006
    """Build a :func:`logging.config.dictConfig` schema dictionary.

    The returned dict can be passed to :func:`logging.config.dictConfig`
    to install handlers, formatters, and per-logger levels.

    Schema overview
    ---------------
    * ``version: 1`` -- mandatory, this is the only supported version.
    * ``disable_existing_loggers: False`` -- preserves loggers that were
      created before this config is applied (e.g., ``app.config``'s
      module logger created at import time).
    * ``formatters`` -- ``plain`` (human-readable text) and ``json``
      (single-line :class:`JsonFormatter` output).
    * ``handlers`` -- a single ``stdout`` :class:`logging.StreamHandler`
      writing to ``sys.stdout`` (the conventional destination for
      twelve-factor apps and containerised deployments; aggregators
      like Docker, Kubernetes, and systemd capture stdout by default).
    * ``loggers`` -- explicit configuration for ``app`` (the application
      hierarchy) and ``werkzeug`` (the development-server request log,
      capped at ``WARNING`` to suppress duplicate request lines).
    * ``root`` -- catch-all handler for any third-party library that
      uses an unregistered logger name.

    Parameters
    ----------
    level : str
        The log level name applied to the ``app`` logger and the root
        logger. Coerced to upper case; falls back to ``"INFO"`` when
        falsy. Valid values: ``"CRITICAL"``, ``"ERROR"``, ``"WARNING"``,
        ``"INFO"``, ``"DEBUG"``, ``"NOTSET"``.
    output_format : str
        Either ``"plain"`` or ``"json"`` (case-insensitive). Anything
        else falls back to ``"plain"`` -- this matches the Node
        convention of ``LOG_FORMAT`` defaulting to a friendly format
        when the value is unrecognised.

    Returns
    -------
    dict
        A new dictionary instance suitable for
        :func:`logging.config.dictConfig`. The returned dict is fresh
        on every call (no shared mutable state).
    """
    # Defensive fallbacks: an empty string or ``None`` becomes ``"INFO"``
    # / ``"plain"``. The truthiness check is intentional -- both empty
    # strings and ``None`` are treated as "use the default".
    level = (level or "INFO").upper()
    formatter_key = "json" if (output_format or "plain").lower() == "json" else "plain"

    return {
        "version": 1,
        "disable_existing_loggers": False,
        "formatters": {
            # Human-readable single-line format. The leading ``%(asctime)s``
            # is rendered using the ``datefmt`` below; the literal ``Z``
            # suffix asserts UTC (operators reading logs in any time zone
            # will correctly interpret the timestamp). The fixed-width
            # ``%(levelname)-8s`` keeps log columns aligned for ``less``,
            # ``grep``, and human eyes.
            "plain": {
                "format": ("%(asctime)s.%(msecs)03dZ " "%(levelname)-8s " "%(name)s: %(message)s"),
                "datefmt": "%Y-%m-%dT%H:%M:%S",
            },
            # JSON formatter -- referenced by dotted import path so that
            # :func:`logging.config.dictConfig` instantiates it at config
            # time. The ``()`` key is the dictConfig convention for
            # "instantiate this callable" (vs. ``class`` which selects
            # a built-in formatter class). Any future kwargs would be
            # added as siblings of ``()``.
            "json": {
                "()": "app.logging_config.JsonFormatter",
            },
        },
        "handlers": {
            # Single stdout handler. ``ext://sys.stdout`` is dictConfig
            # syntax that resolves to the ``sys.stdout`` attribute at
            # config time. Writing to stdout (rather than stderr or a
            # file) follows twelve-factor methodology -- the operator's
            # supervisor (gunicorn, systemd, Docker, Kubernetes) captures
            # stdout and routes it to the configured aggregator. The
            # handler-level cap ensures even root-logger messages from
            # third parties respect the configured level.
            "stdout": {
                "class": "logging.StreamHandler",
                "stream": "ext://sys.stdout",
                "formatter": formatter_key,
                "level": level,
            },
        },
        "loggers": {
            # Application logger. Every blueprint, service, model, and
            # utility module in this project obtains its logger via
            # ``logging.getLogger(__name__)``, producing names of the
            # form ``app.<subpackage>.<module>``. Configuring the
            # ``app`` parent governs the entire subtree. ``propagate``
            # is False so that records are NOT also emitted by the root
            # logger (which would produce duplicates).
            "app": {
                "handlers": ["stdout"],
                "level": level,
                "propagate": False,
            },
            # Werkzeug request log. The Werkzeug development server emits
            # one INFO-level line per request (``"GET /health HTTP/1.1" 200``).
            # In a Flask app these are usually redundant with whatever
            # access-log middleware the application installs; capping at
            # WARNING suppresses them in dev. In production under
            # gunicorn this logger is not triggered at all -- gunicorn
            # provides its own ``--access-logfile`` flag. Setting
            # ``propagate: False`` prevents werkzeug records from
            # propagating to the root logger and being emitted twice
            # via the root handler chain.
            "werkzeug": {
                "handlers": ["stdout"],
                "level": "WARNING",
                "propagate": False,
            },
        },
        # Root logger catches anything not matched by a specific logger
        # config. Useful so that records from third-party libraries
        # (requests, urllib3, sqlalchemy, etc.) are visible at the
        # configured level rather than dropping silently. The root
        # handler list intentionally points at the same stdout handler
        # so all log output ends up in one stream.
        "root": {
            "handlers": ["stdout"],
            "level": level,
        },
    }


# ---------------------------------------------------------------------------
# Public entry point invoked by :func:`app.create_app`.
# ---------------------------------------------------------------------------


def configure_logging(app: Flask) -> None:
    """Apply :func:`logging.config.dictConfig` based on the Flask
    application's configuration.

    Reads ``app.config['LOG_LEVEL']`` (default ``"INFO"``) and
    ``app.config['LOG_FORMAT']`` (default ``"plain"``), constructs the
    schema via :func:`_build_dict_config`, and installs it via
    :func:`logging.config.dictConfig`. After installation, emits a
    single ``INFO``-level confirmation line so operators can verify in
    the captured logs that configuration ran.

    Idempotency
    -----------
    :func:`logging.config.dictConfig` is idempotent: calling it
    repeatedly with the same schema replaces the previous handlers
    cleanly (no handler accumulation). This makes the function safe to
    invoke multiple times in test suites that build several Flask
    applications via :func:`app.create_app`.

    Side-effects
    ------------
    This function mutates **process-global** logging state. It must be
    called from the main thread before any worker threads or request
    contexts exist. Calling it from a request handler is undefined
    behaviour.

    Parameters
    ----------
    app : flask.Flask
        The Flask application instance whose ``config`` dict is
        consulted. Must not be ``None``.

    Returns
    -------
    None
        This function returns nothing -- the only observable effect is
        the mutated global logging state and the single confirmation
        log line emitted at the end.

    Raises
    ------
    ValueError
        Raised by :func:`logging.config.dictConfig` if the schema is
        malformed (e.g., an unknown handler class). The schema produced
        by :func:`_build_dict_config` is hand-verified, so this should
        only fire if a future refactor introduces a typo.

    Examples
    --------
    Typical use inside the application factory::

        from flask import Flask

        from app.config import DevelopmentConfig
        from app.logging_config import configure_logging


        def create_app() -> Flask:
            app = Flask(__name__)
            app.config.from_object(DevelopmentConfig)
            configure_logging(app)
            # ... init extensions, register blueprints, etc. ...
            return app
    """
    # Read the values from app.config. ``app.config`` is a dict subclass
    # populated by ``app.config.from_object(SomeConfig)``; the values
    # come ultimately from environment variables (see :mod:`app.config`).
    # ``str(...)`` coerces in case the operator overrode the value with
    # a non-string sentinel (defensive -- ``app.config.get`` already
    # returns whatever was stored, but the helper expects strings).
    level = app.config.get("LOG_LEVEL", "INFO")
    output_format = app.config.get("LOG_FORMAT", "plain")
    schema = _build_dict_config(
        level=str(level),
        output_format=str(output_format),
    )

    # Apply the schema. This mutates the global logging state.
    logging.config.dictConfig(schema)

    # Emit a single confirmation line so operators can verify in the
    # captured stdout that configuration completed and which level /
    # format were applied. We use this module's own logger (which is
    # now governed by the freshly applied schema) and string-format
    # the values via the logging framework's lazy %-substitution
    # (so the formatting only happens if the level passes -- but at
    # INFO this always fires).
    logger.info(
        "Logging configured (level=%s, format=%s, handler=stdout).",
        level,
        output_format,
    )


# ---------------------------------------------------------------------------
# Public symbol export.
#
# ``JsonFormatter`` is exported even though application code does not
# import it directly: the dictConfig schema in :func:`_build_dict_config`
# references it via the dotted path ``"app.logging_config.JsonFormatter"``,
# so it MUST be reachable at module scope under that exact name. Hiding
# it behind a leading underscore would break the schema.
# ``configure_logging`` is the public entry point invoked by
# :func:`app.create_app`. ``_build_dict_config`` is intentionally NOT
# exported (the leading underscore signals private), but it remains
# importable via its dotted name for unit tests.
# ---------------------------------------------------------------------------
__all__ = ["JsonFormatter", "configure_logging"]
