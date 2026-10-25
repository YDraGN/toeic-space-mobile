# Security policy

Please **do not** open a public issue for security problems (for example token leaks, authentication bypass, or exposed test answers).

Report them privately through GitHub: **Security → Report a vulnerability** on this repository. Include steps to reproduce and the app version. We aim to respond within 7 days.

Never commit secrets: `local.properties`, keystores (`*.jks`, `*.keystore`), `google-services.json`, or real access tokens in code and tests.
