"""Flask configuration class hierarchy.

Defines :class:`Config` and three subclasses (``DevelopmentConfig``,
``TestingConfig``, ``ProductionConfig``) selected by the
``FLASK_CONFIG`` environment variable. The selected class is loaded
by :func:`app.create_app` via ``app.config.from_object(cls)``.

All values default to safe placeholders. Production deployments MUST
override at least ``SECRET_KEY``; the constructor will raise if it is
left at the default in :class:`ProductionConfig`.

Per AAP Rule R6, environment variables are read with explicit type
coercion (``int``, ``bool``) so that string values from the OS are
converted before reaching Flask's config dict.

Architectural notes
-------------------
* Flask's :meth:`flask.Config.from_object` reads ONLY ``UPPER_CASE``
  class attributes via :func:`getattr`. Properties, classmethods, and
  instance attributes are NOT picked up. This is why each class is
  used purely as a namespace of constants, with no ``@property`` or
  similar dynamic access.
* Environment variables are read at IMPORT time, not at
  ``from_object`` time. This matches the Node.js convention where
  ``const PORT = process.env.PORT || 3000;`` runs once at module
  load. Tests that need to override env vars MUST do so before the
  first ``import app.config`` (or use ``importlib.reload``).
* :class:`ProductionConfig` has both an ``__init__`` and a
  classmethod ``_validate``. The classmethod is what the
  application factory (``app/__init__.py``) calls after
  ``app.config.from_object(ProductionConfig)``, because
  ``from_object`` does NOT instantiate the class. The ``__init__``
  is a defensive convenience for callers that explicitly do
  ``ProductionConfig()``.
* Database, Redis, JWT, CORS, and similar third-party-driven
  settings are deliberately omitted at this scaffold stage. They are
  added per AAP Section 0.6.1.2 once the original Node.js source is
  supplied — adding them prematurely would invent values that do not
  exist yet (which would violate AAP Rule 9 — Translate, do not
  optimize).

This module imports ONLY Python standard library symbols. No Flask,
no third-party package; this prevents circular imports with
:mod:`app.__init__` which itself imports configuration classes from
here.
"""

from __future__ import annotations

import os
from pathlib import Path
from typing import Optional

# ----------------------------------------------------------------------
# Module-level helpers for explicit environment-variable coercion.
# ----------------------------------------------------------------------

# Set of strings (lower-cased and stripped) recognised as truthy by
# :func:`_env_bool`. Anything else is falsy. Mirrors the conventions
# used by Django, Flask-Cors, and many other Python libraries so that
# operators familiar with one will not be surprised by the other.
_TRUTHY: frozenset[str] = frozenset({"1", "true", "yes", "on", "y", "t"})


def _env_bool(name: str, default: bool = False) -> bool:
    """Read an environment variable and coerce it to a Python ``bool``.

    Empty strings and unset variables return ``default``. Recognised
    truthy strings (case-insensitive, after stripping whitespace):
    ``"1"``, ``"true"``, ``"yes"``, ``"on"``, ``"y"``, ``"t"``.
    Anything else returns ``False``.

    The explicit truthy/falsy mapping avoids the common Python bug
    where ``bool(os.environ.get("DEBUG", "false"))`` evaluates to
    ``True`` because the string ``"false"`` is non-empty and Python
    considers any non-empty string truthy.

    Parameters
    ----------
    name:
        Environment variable name (e.g., ``"FLASK_DEBUG"``).
    default:
        Returned when the variable is unset or empty. Default
        ``False``.

    Returns
    -------
    bool
        ``True`` if the variable's value is in ``_TRUTHY`` (after
        case-folding and stripping); ``False`` otherwise; ``default``
        when the variable is unset or empty.
    """
    raw = os.environ.get(name)
    if raw is None or raw == "":
        return default
    return raw.strip().lower() in _TRUTHY


def _env_int(name: str, default: int) -> int:
    """Read an environment variable and coerce it to a Python ``int``.

    Empty strings, unset variables, and non-integer strings return
    ``default``. Non-integer strings emit no warning here; downstream
    consumers (Pydantic, SQLAlchemy, etc.) will surface their own
    validation errors when the value is later used in a context that
    requires a specific shape.

    This mirrors the Node.js convention
    ``parseInt(process.env.PORT) || 3000`` returning ``3000`` even when
    ``process.env.PORT === "abc"``.

    Parameters
    ----------
    name:
        Environment variable name (e.g., ``"PORT"``).
    default:
        Returned when the variable is unset, empty, or not parseable
        as an integer.

    Returns
    -------
    int
        The parsed integer value, or ``default`` on any failure.
    """
    raw = os.environ.get(name)
    if raw is None or raw == "":
        return default
    try:
        return int(raw)
    except (ValueError, TypeError):
        return default


# ----------------------------------------------------------------------
# Filesystem path constants computed once at import.
# ----------------------------------------------------------------------

# Repository root — the directory containing ``app/``, ``tests/``,
# ``scripts/``, ``wsgi.py``. Computed by walking two parents up from
# this file: ``app/config.py`` -> ``app/`` -> repository root.
PROJECT_ROOT: Path = Path(__file__).resolve().parent.parent

# Flask's instance folder convention: ``project_root/instance/``.
# Created on demand by :func:`app.create_app`. Holds environment-
# specific files (uploaded files, SQLite databases, instance-specific
# config overrides) that should NOT be committed to version control.
INSTANCE_DIR: Path = PROJECT_ROOT / "instance"


# ----------------------------------------------------------------------
# Base configuration class — all defaults live here.
# ----------------------------------------------------------------------


class Config:
    """Base configuration with safe defaults for all environments.

    Subclasses override only what differs per environment. Attribute
    names MUST be ``UPPER_CASE`` because Flask's
    :meth:`flask.Config.from_object` only loads uppercase attributes.

    The constants exposed here cover:

    * **Flask core** — ``SECRET_KEY``, ``DEBUG``, ``TESTING``,
      ``JSON_SORT_KEYS``, ``MAX_CONTENT_LENGTH``,
      ``PROPAGATE_EXCEPTIONS``.
    * **HTTP server bind** — ``HOST``, ``PORT``, ``WEB_CONCURRENCY``.
    * **Logging** — ``LOG_LEVEL``, ``LOG_FORMAT``.
    * **Reverse-proxy trust** — ``TRUSTED_PROXY_COUNT``.
    * **Filesystem paths** — ``PROJECT_ROOT``, ``INSTANCE_DIR``.

    Domain-specific keys (``DATABASE_URL``, ``REDIS_URL``,
    ``CORS_ALLOW_ORIGINS``, ``JWT_SECRET``, etc.) are intentionally
    omitted; they will be added per AAP Section 0.6.1.2 when the
    original Node.js source is supplied.
    """

    # ------------------------------------------------------------------
    # Flask core
    # ------------------------------------------------------------------

    # Secret used by Flask sessions and any ``itsdangerous``-signed
    # tokens. ``ProductionConfig._validate`` enforces that this is
    # overridden via the ``SECRET_KEY`` env var before production
    # deploys; the placeholder default lets the dev server boot
    # without any environment configuration.
    SECRET_KEY: str = os.environ.get(
        "SECRET_KEY",
        "change-me-to-a-long-random-string",
    )

    # Whether Flask runs in debug mode (interactive debugger, auto
    # reloader). Subclasses override explicitly.
    DEBUG: bool = False

    # Whether Flask runs in testing mode (more error propagation,
    # cleaner test client). Subclasses override explicitly.
    TESTING: bool = False

    # Whether :func:`flask.jsonify` sorts keys in JSON responses.
    # ``True`` matches Flask's pre-2.3 default and helps make
    # responses byte-equivalent across runs (useful for snapshot
    # tests and HTTP-cache validators).
    JSON_SORT_KEYS: bool = True

    # Maximum allowed request body size in bytes (16 MiB). Bodies
    # larger than this trigger Flask's automatic 413 (Request Entity
    # Too Large) response. Configurable via the
    # ``MAX_CONTENT_LENGTH`` environment variable.
    MAX_CONTENT_LENGTH: int = _env_int("MAX_CONTENT_LENGTH", 16 * 1024 * 1024)

    # Whether to propagate exceptions out of view functions so that
    # tools like the Werkzeug interactive debugger can catch them.
    # ``None`` means "Flask decides" (it follows ``DEBUG`` /
    # ``TESTING``); subclasses set explicit booleans where they want
    # deterministic behaviour.
    #
    # ``Optional[bool]`` (rather than ``bool | None``) is used
    # deliberately so the imported ``typing.Optional`` symbol is
    # actually referenced; the per-line ``# noqa: UP007`` opts out of
    # ruff's UP007 modernisation rule (the schema for this file
    # mandates the explicit ``Optional`` import).
    PROPAGATE_EXCEPTIONS: Optional[bool] = None  # noqa: UP007

    # ------------------------------------------------------------------
    # HTTP server bind
    # ------------------------------------------------------------------

    # Bind address for the development server (``flask run``) and the
    # production WSGI host (``gunicorn -b ${HOST}:${PORT}``). Use
    # ``127.0.0.1`` for local development, ``0.0.0.0`` for
    # containerised production.
    HOST: str = os.environ.get("HOST", "127.0.0.1")

    # Bind port. Must match the port any reverse proxy or load
    # balancer is forwarding to.
    PORT: int = _env_int("PORT", 5000)

    # Number of gunicorn worker processes (production only). A common
    # rule of thumb is ``(2 * CPU_count) + 1``.
    WEB_CONCURRENCY: int = _env_int("WEB_CONCURRENCY", 4)

    # ------------------------------------------------------------------
    # Logging
    # ------------------------------------------------------------------

    # Python logging level for the application logger. One of
    # ``"CRITICAL"``, ``"ERROR"``, ``"WARNING"``, ``"INFO"``,
    # ``"DEBUG"``. Values are upper-cased so case-insensitive input
    # from the env var works.
    LOG_LEVEL: str = os.environ.get("LOG_LEVEL", "INFO").upper()

    # Log output format. ``"plain"`` for human-readable lines,
    # ``"json"`` for machine-parseable JSON consumed by ELK,
    # Datadog, CloudWatch, and similar log pipelines. Lower-cased so
    # case-insensitive input from the env var works.
    LOG_FORMAT: str = os.environ.get("LOG_FORMAT", "plain").lower()

    # ------------------------------------------------------------------
    # Reverse-proxy trust
    # ------------------------------------------------------------------

    # Number of trusted reverse-proxy hops in front of the
    # application. Used by :class:`werkzeug.middleware.proxy_fix.
    # ProxyFix` (wired in :func:`app.create_app`) to decide how many
    # ``X-Forwarded-*`` header values to honour. Set to ``0`` when
    # the app is exposed directly, ``1`` when behind a single proxy
    # (nginx, traefik, AWS ALB), ``2`` when behind two
    # (e.g., CloudFront -> ALB).
    TRUSTED_PROXY_COUNT: int = _env_int("TRUSTED_PROXY_COUNT", 0)

    # ------------------------------------------------------------------
    # Filesystem paths
    # ------------------------------------------------------------------

    # Re-exposed at class scope so that
    # ``current_app.config["PROJECT_ROOT"]`` and
    # ``current_app.config["INSTANCE_DIR"]`` work uniformly anywhere
    # a Flask request context is active.
    PROJECT_ROOT: Path = PROJECT_ROOT
    INSTANCE_DIR: Path = INSTANCE_DIR


# ----------------------------------------------------------------------
# DevelopmentConfig — local interactive development.
# ----------------------------------------------------------------------


class DevelopmentConfig(Config):
    """Configuration for local interactive development.

    ``DEBUG`` enabled, ``TESTING`` disabled, log level ``DEBUG``,
    log format ``plain``. Exceptions propagate so the Werkzeug
    interactive debugger triggers on unhandled errors.
    """

    DEBUG: bool = True
    TESTING: bool = False

    # In dev we default to verbose logs but allow override via
    # ``LOG_LEVEL`` so a developer can quiet the logger when they
    # want to focus on a specific code path.
    LOG_LEVEL: str = os.environ.get("LOG_LEVEL", "DEBUG").upper()
    LOG_FORMAT: str = os.environ.get("LOG_FORMAT", "plain").lower()

    # In dev, propagate exceptions so the Werkzeug debugger triggers
    # on every unhandled error rather than producing a generic 500.
    PROPAGATE_EXCEPTIONS: Optional[bool] = True  # noqa: UP007


# ----------------------------------------------------------------------
# TestingConfig — pytest test suite.
# ----------------------------------------------------------------------


class TestingConfig(Config):
    """Configuration for the pytest test suite.

    ``TESTING`` enabled (Flask suppresses some error catching, exposes
    the test client more cleanly), ``DEBUG`` disabled, propagation
    enabled so tests can assert on raised exceptions, log level
    ``WARNING`` to keep test output clean.

    ``SECRET_KEY`` is set to a deterministic test value so signed
    cookies and tokens are reproducible across runs.
    """

    DEBUG: bool = False
    TESTING: bool = True

    # Deterministic test secret — never use this in production. The
    # constant value makes signed-cookie and signed-token assertions
    # reproducible across test runs.
    SECRET_KEY: str = "test-secret-key-not-for-production"

    # Hard-coded so that env vars set by the test runner do not leak
    # into and pollute test output. ``WARNING`` suppresses the
    # ``INFO``-level access logs that production code emits.
    LOG_LEVEL: str = "WARNING"
    LOG_FORMAT: str = "plain"

    # Tests need to assert on raised exceptions, so we ALWAYS
    # propagate (never let Flask convert them into 500 responses).
    PROPAGATE_EXCEPTIONS: Optional[bool] = True  # noqa: UP007

    # Disable Flask-WTF's CSRF protection in the test environment.
    # When ``Flask-WTF`` is later added to the project, this flag
    # ensures tests don't have to scrape and resubmit CSRF tokens on
    # every form post. For the current scaffold (no Flask-WTF) the
    # attribute is a forward-looking placeholder that does no harm.
    WTF_CSRF_ENABLED: bool = False


# ----------------------------------------------------------------------
# ProductionConfig — production deployments.
# ----------------------------------------------------------------------


class ProductionConfig(Config):
    """Configuration for production deployments.

    ``DEBUG`` and ``TESTING`` disabled, log level ``INFO``, log format
    ``json`` (for log aggregators), ``PROPAGATE_EXCEPTIONS`` ``False``
    so unhandled exceptions become 500 responses (the error handler
    registered in :mod:`app.errors` produces the public-facing error
    body).

    ``SECRET_KEY`` validation
    -------------------------
    Flask's :meth:`flask.Config.from_object` does NOT instantiate this
    class — it merely reads class attributes. So the ``__init__``
    defined below is NOT called by the application factory in the
    normal flow.

    To enforce that ``SECRET_KEY`` has been overridden via the
    environment, the application factory in :mod:`app.__init__`
    explicitly calls :meth:`ProductionConfig._validate` after
    ``app.config.from_object(ProductionConfig)``. The expected
    pattern is::

        if config_name == "production" and hasattr(config_class, "_validate"):
            config_class._validate()

    The ``__init__`` is provided as a defensive convenience for
    callers that *do* instantiate ``ProductionConfig`` directly
    (e.g., custom scripts that build a config object before passing
    it to ``app.config.from_object``).
    """

    DEBUG: bool = False
    TESTING: bool = False

    # Production logs default to ``INFO`` (everything important, no
    # noise) and ``json`` (machine-parseable for ELK / Datadog /
    # CloudWatch). Both can be overridden via env vars without code
    # changes.
    LOG_LEVEL: str = os.environ.get("LOG_LEVEL", "INFO").upper()
    LOG_FORMAT: str = os.environ.get("LOG_FORMAT", "json").lower()

    # Never propagate exceptions in production: the error handler
    # registered in :mod:`app.errors` is the single source of truth
    # for the response body and status code.
    PROPAGATE_EXCEPTIONS: Optional[bool] = False  # noqa: UP007

    def __init__(self) -> None:
        """Validate configuration when the class is explicitly
        instantiated.

        Note that Flask's ``app.config.from_object`` does not call
        this — see the class-level docstring. The application factory
        (``app/__init__.py``) calls :meth:`_validate` separately after
        ``from_object`` to ensure validation runs in the canonical
        Flask-loading flow.
        """
        self._validate()

    @classmethod
    def _validate(cls) -> None:
        """Raise :class:`RuntimeError` if production has been deployed
        without a real ``SECRET_KEY``.

        Called explicitly by :func:`app.create_app` after
        ``app.config.from_object(ProductionConfig)``.

        Raises
        ------
        RuntimeError
            When :attr:`SECRET_KEY` is the placeholder value
            ``"change-me-to-a-long-random-string"`` shipped in
            :class:`Config`.
        """
        if cls.SECRET_KEY == "change-me-to-a-long-random-string":
            raise RuntimeError(
                "ProductionConfig requires SECRET_KEY to be overridden "
                "via the SECRET_KEY environment variable. Generate one with: "
                "python -c 'import secrets; print(secrets.token_urlsafe(48))'"
            )


# ----------------------------------------------------------------------
# Public symbol export.
# ----------------------------------------------------------------------

__all__ = [
    "Config",
    "DevelopmentConfig",
    "TestingConfig",
    "ProductionConfig",
    "PROJECT_ROOT",
    "INSTANCE_DIR",
]
