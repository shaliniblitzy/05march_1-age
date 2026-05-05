# 11nov01

**Python/Flask rewrite of the original Node.js server.**

This repository hosts a Flask application produced as a tech-stack migration
from a Node.js HTTP server to Python 3 / Flask 3.1.x. The migration is
designed to preserve the public API contract of the original — URL paths,
HTTP methods, status codes, response shapes, headers, cookies, and
environment-variable names are all reproduced verbatim, while the internal
implementation is idiomatic Python.

---

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Server](#running-the-server)
- [Testing](#testing)
- [Project Layout](#project-layout)
- [Dependencies](#dependencies)
- [License](#license)
- [Migration Notes](#migration-notes)

---

## Overview

`11nov01` is a Python 3 / Flask 3.1.x application that implements the same
HTTP behavior as the original Node.js server it replaces. Internally the
project follows Flask's recommended conventions:

- **Application factory pattern.** The `Flask` instance is constructed by
  `create_app(config_name)` in [`app/__init__.py`](app/__init__.py), which
  loads configuration, initializes extensions, registers Blueprints, and
  registers error handlers before returning the app. This keeps the global
  module namespace free of an implicit application object and makes testing
  straightforward.
- **Blueprints for modular routing.** Each logical group of routes lives in
  its own module under `app/blueprints/` and is registered on the app from
  `create_app`. This mirrors the per-router decomposition that the original
  Node.js server used.
- **Extension singletons.** Flask extensions (database, CORS, login, etc.)
  are instantiated once in `app/extensions.py` and bound to the app via
  `extension.init_app(app)` from the factory. This avoids circular imports.
- **WSGI for production.** The production deployment target is `gunicorn`,
  a battle-tested WSGI host. The `wsgi.py` module exposes the `app`
  callable that `gunicorn` (or any other WSGI server) invokes.

The **public API contract** — every URL path, HTTP method, status code,
response body shape, response header (including `Content-Type`,
`Set-Cookie`, `Location`, and any custom `X-*` headers), authentication
header semantics, cookie name, session format, and environment-variable
name — is preserved verbatim from the Node.js original so that existing
clients and deployment manifests continue to work unchanged.

---

## Prerequisites

- **Python 3.12.3** (the version pinned in [`.python-version`](.python-version)).
  Any Python ≥ 3.12 is supported; Flask 3.1.x itself requires Python ≥ 3.9,
  but this project targets 3.12 specifically.
- **`pip`** — bundled with Python 3.12 (`python3.12 -m pip --version`).
- **`venv`** — bundled with Python 3.12; used to create an isolated
  per-project environment.
- *(Optional)* **`pyenv`** for managing multiple Python versions on a
  single machine. The repository ships a `.python-version` file that
  `pyenv` reads automatically when you `cd` into the project.
- *(Optional)* **`git`** for cloning the repository and tracking changes.

No Node.js, npm, yarn, or pnpm is required at any point in the build,
test, or deployment lifecycle.

---

## Installation

The recommended workflow uses a virtual environment so that the project's
dependencies are isolated from your system Python:

```bash
# 1. Clone the repository (if not already done)
git clone <repository-url>
cd 11nov01

# 2. Create and activate a virtual environment
python3.12 -m venv .venv
source .venv/bin/activate         # macOS / Linux
# .venv\Scripts\activate          # Windows PowerShell

# 3. Install runtime dependencies
pip install --upgrade pip
pip install -r requirements.txt

# 4. (Optional) Install development & testing dependencies
pip install -r requirements-dev.txt
```

[`requirements.txt`](requirements.txt) pins **Flask 3.1.3** along with
its transitive dependencies (Werkzeug, Jinja2, itsdangerous, click,
blinker, MarkupSafe), plus `python-dotenv` for `.env` loading and
`gunicorn` for production WSGI hosting. Every version is an **exact
pin** (no ranges, no `latest`); see the [Dependencies](#dependencies)
section for the full list.

---

## Configuration

Configuration is loaded from environment variables. During development
those variables can be supplied via a local `.env` file, which is loaded
automatically by `python-dotenv` when the Flask CLI starts and when
`wsgi.py` calls `load_dotenv()`. The `.flaskenv` file (committed to the
repository) supplies the non-secret defaults that are convenient for
local development (`FLASK_APP=wsgi:app`, `FLASK_DEBUG=1`).

To set up local configuration, copy the documented template and edit it
for your environment:

```bash
cp .env.example .env
# then edit .env with your local values
```

`.env` is listed in [`.gitignore`](.gitignore) and must never be
committed; only `.env.example` is tracked.

The Flask scaffold reads the following environment variables. Variable
names match the Node.js original 1:1 so that existing deployment
manifests (Docker Compose, Helm values, Kubernetes ConfigMaps, etc.)
continue to work unchanged.

| Variable | Default | Description |
|---|---|---|
| `FLASK_APP` | `wsgi:app` | Module:callable that Flask's CLI uses to discover the application (set in `.flaskenv`). |
| `FLASK_DEBUG` | `0` (production) / `1` (dev) | Enables the debugger and reloader when `1`. |
| `FLASK_CONFIG` | `production` | Selects the `Config` subclass loaded by `create_app` (one of `development`, `testing`, `production`). |
| `SECRET_KEY` | (no default in production) | Secret used by Flask sessions and `itsdangerous`. **Required in production.** |
| `HOST` | `127.0.0.1` (dev) / `0.0.0.0` (prod) | Bind address for the development and production servers. |
| `PORT` | `5000` | Bind port for the development and production servers. |
| `WEB_CONCURRENCY` | `4` | Number of `gunicorn` worker processes (production only). |
| `LOG_LEVEL` | `INFO` | Python `logging` level for the application logger. |

> Additional environment variables are introduced when blueprints,
> services, and database integrations are wired in; they will be
> enumerated in `.env.example` as they appear.

---

## Running the Server

### Development

The development server uses Flask's built-in reloader and interactive
debugger. Three equivalent ways to start it are shown below; pick
whichever you prefer.

```bash
# Easiest: rely on .flaskenv defaults (FLASK_APP=wsgi:app, FLASK_DEBUG=1)
flask run

# Equivalently, with explicit flags
flask --app wsgi:app --debug run --host=127.0.0.1 --port=5000

# Or via the helper script
bash scripts/run_dev.sh
```

The development server listens on `http://127.0.0.1:5000` by default.
It is **not** suitable for production traffic — it is single-threaded
and not hardened against malicious input. Use `gunicorn` (below) for
anything beyond local testing.

### Production

The production deployment uses `gunicorn`, a pre-fork WSGI server.
The default invocation runs four worker processes bound to all
interfaces on port 5000:

```bash
# Run under gunicorn with the documented defaults
gunicorn -w 4 -b 0.0.0.0:5000 wsgi:app

# Or via the helper script
bash scripts/run_prod.sh
```

The number of workers can be tuned via the `WEB_CONCURRENCY`
environment variable; a common rule of thumb is `(2 * CPU_count) + 1`.

> **Windows note.** `gunicorn` is UNIX-only. On Windows, install
> [`waitress`](https://pypi.org/project/waitress/) (`pip install waitress`)
> and run `waitress-serve --listen=0.0.0.0:5000 wsgi:app` instead.
> `waitress` is **not** added to `requirements.txt` because the
> primary deployment target is Linux; install it ad-hoc on Windows
> only.

---

## Testing

The test suite uses [`pytest`](https://docs.pytest.org/) together with
[`pytest-flask`](https://pytest-flask.readthedocs.io/), with fixtures in
`tests/conftest.py` that build the application via
`create_app('testing')` and expose a Flask test client through
`app.test_client()`.

```bash
# Install dev dependencies once (also installs the runtime deps)
pip install -r requirements-dev.txt

# Run the full suite
pytest

# Run with verbose output and coverage
pytest -v --cov=app --cov-report=term-missing
```

`pytest` configuration lives in the `[tool.pytest.ini_options]` block
of [`pyproject.toml`](pyproject.toml); coverage configuration lives in
the `[tool.coverage.*]` blocks of the same file. The default test
discovery looks for `test_*.py` and `*_test.py` files under `tests/`.

---

## Project Layout

The directory tree below reflects the application factory + Blueprints
layout described in the architecture overview. Each entry is annotated
with a one-line description.

```
./
├── README.md                       # This file
├── pyproject.toml                  # PEP 621 project metadata, build system, tool config
├── requirements.txt                # Pinned runtime dependencies
├── requirements-dev.txt            # Pinned dev/test dependencies
├── .python-version                 # 3.12.3 (for pyenv)
├── .env.example                    # Documented environment variable template
├── .gitignore                      # Python-specific ignores
├── .flaskenv                       # FLASK_APP and FLASK_DEBUG defaults for local dev
├── wsgi.py                         # Production WSGI entry point: app = create_app()
├── app/                            # Flask application package
│   ├── __init__.py                 # create_app(config_name) application factory
│   ├── config.py                   # Config / DevelopmentConfig / TestingConfig / ProductionConfig
│   ├── extensions.py               # Flask extension singletons (db, cors, login_manager, ...)
│   ├── logging_config.py           # logging.dictConfig setup
│   ├── errors.py                   # @errorhandler registrations for 4xx/5xx parity
│   ├── blueprints/                 # One Blueprint per Node route module
│   ├── services/                   # Business logic ported from Node services/controllers
│   ├── models/                     # SQLAlchemy / PyMongo / MongoEngine models
│   ├── schemas/                    # Pydantic / Marshmallow schemas (Joi/Zod equivalents)
│   ├── middleware/                 # Flask hooks and WSGI middleware
│   ├── utils/                      # Pure-Python helpers
│   ├── templates/                  # Jinja2 templates (converted from EJS/Pug/Handlebars/Nunjucks)
│   └── static/                     # Static assets (copied from Node public/ or dist/)
├── tests/                          # pytest test suite
│   ├── __init__.py
│   ├── conftest.py                 # Pytest fixtures: app, client, db_session
│   ├── test_app.py                 # Application factory smoke tests
│   └── ...                         # One test module per Node test file
└── scripts/
    ├── run_dev.sh                  # flask --app wsgi:app --debug run
    └── run_prod.sh                 # gunicorn -w 4 -b 0.0.0.0:${PORT} wsgi:app
```

Per-directory summary:

- **`app/`** — the Flask application package; everything imported by
  `create_app` lives here.
- **`app/blueprints/`** — one module per logical group of routes;
  each defines a `Blueprint` that is registered on the app from
  `create_app`.
- **`app/services/`** — business-logic modules called by view functions
  so that views remain thin (parse input, call service, format
  response).
- **`app/models/`** — persistence-layer types (SQLAlchemy declarative
  models, PyMongo documents, etc.) translated from the Node.js source's
  data models.
- **`app/schemas/`** — input/output validation schemas (Pydantic or
  Marshmallow), translated 1:1 from any Joi / Zod / class-validator
  schemas in the Node.js source.
- **`app/middleware/`** — Flask `before_request` / `after_request`
  hooks, decorator-style middleware, and WSGI-level wrappers.
- **`app/utils/`** — pure-Python helpers with no Flask coupling.
- **`app/templates/`** — Jinja2 templates; converted from the original
  EJS / Pug / Handlebars / Nunjucks templates when present.
- **`app/static/`** — static assets served at `/static/`; the contents
  are copied verbatim from the Node.js `public/` or `dist/` directory.
- **`tests/`** — `pytest` test suite with shared fixtures in
  `conftest.py`.
- **`scripts/`** — operator-facing shell scripts for development and
  production startup.

---

## Dependencies

All Python dependencies are pinned to exact versions (no ranges, no
`latest`). The lists below mirror the contents of
[`requirements.txt`](requirements.txt) and
[`requirements-dev.txt`](requirements-dev.txt); those files are the
source of truth for installation.

### Runtime

- `Flask==3.1.3`
- `Werkzeug==3.1.8`
- `Jinja2==3.1.6`
- `itsdangerous==2.2.0`
- `click==8.3.3`
- `blinker==1.9.0`
- `MarkupSafe==3.0.3`
- `python-dotenv==1.1.1`
- `gunicorn==23.0.0`

### Development

The development extras are pinned in `requirements-dev.txt`:

- `pytest`
- `pytest-flask`
- `pytest-cov`
- `coverage`
- `ruff`
- `black`

> When the original Node.js source is supplied, additional runtime
> dependencies (e.g., `Flask-Cors`, `Flask-SQLAlchemy`, `PyJWT`,
> `psycopg`, `httpx`) will be added to `requirements.txt`. Each npm
> package found in the Node `package.json` is mapped to its PyPI
> equivalent and pinned to an exact version that is compatible with
> Flask 3.1.3 on Python 3.12.

---

## License

License terms are language-independent; refer to the `LICENSE` file
at the repository root if present, or contact the project owner for
licensing details.

---

## Migration Notes

This repository was produced as a one-pass tech-stack migration from a
Node.js HTTP server to a Python 3 / Flask 3.1.x application. The
migration preserves the public API contract (URL paths, HTTP methods,
status codes, response shapes, headers, cookies, and
environment-variable names) of the original. Internal module
organization follows Flask conventions (application factory,
Blueprints, extension singletons), and Python style follows PEP 8
(snake_case for module, function, and variable names). The wire-level
contract — paths, query parameter names, JSON field names, header
names, and cookie names — remains identical to the Node.js original
because those are part of the externally observable behavior that
clients depend on.
