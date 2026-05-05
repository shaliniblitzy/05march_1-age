"""Pure-Python utility helpers for the Flask application.

This package houses plain Python utility functions translated 1:1 from
Node.js helper modules (``utils/``, ``helpers/``, ``lib/``, or
co-located ``*.helper.js`` files). Helpers in this package MUST be
pure functions:

* No Flask request access (``request``, ``current_app``, ``g``).
* No I/O side effects beyond what their original Node counterpart did.
* No business logic — that lives in :mod:`app.services`.
* No model interactions — that lives in :mod:`app.services` /
  :mod:`app.models`.

Examples include date/time formatters, string manipulators, validators,
encoders/decoders, and small mathematical helpers.

Imports are limited to the Python standard library and the project's
own modules (per AAP Section 0.6.2.1). When a Node helper imports a
specific npm package, the corresponding PyPI dependency is added to
``requirements.txt`` per AAP Section 0.6.1.2.

In the current scaffold (no Node source supplied), this package
contains only this package marker. Helper modules will be added 1:1
per AAP Section 0.5.1.2 Part B when the original Node.js source is
provided; downstream agents will create one ``app/utils/<helper>.py``
module per Node helper file using ``snake_case`` naming.
"""
