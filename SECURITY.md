# Security Policy

## Reporting a Vulnerability

If you believe you have discovered a security vulnerability in Stew Android, please report it privately rather than opening a public GitHub issue.

Please contact the Stew maintainers with a detailed description of the vulnerability.

### What to include

When possible, please include:

* A description of the vulnerability
* The affected version, feature, or component
* Steps to reproduce the issue
* The potential security impact
* Relevant logs, screenshots, or proof-of-concept code
* Any suggested remediation

Please do **not** include passwords, OAuth tokens, refresh tokens, API keys, client secrets, or other sensitive credentials in your report.

## Do Not Publicly Disclose Vulnerabilities

Please do not publicly disclose a security vulnerability before the Stew maintainers have had a reasonable opportunity to investigate and address it.

Do not use public GitHub issues, pull requests, or discussions to report security vulnerabilities.

## Authentication and OAuth

Stew Android communicates with Roblox through the Stew API.

The Roblox OAuth client secret must never be included in the Android application, its source code, APK, or any distributed application package.

OAuth credentials, authorization codes, access tokens, and refresh tokens should be handled securely and should not be written to logs or publicly exposed.

## Local Credential Storage

Authentication tokens stored on the Android device should use appropriate platform security mechanisms.

Security-sensitive data should not be stored in plaintext when a secure storage mechanism is available.

Users should never be asked to provide their Roblox password directly to Stew.

## Deep Links and OAuth Callbacks

Stew uses the `stew://oauth/callback` URI for completing the OAuth authorization flow.

OAuth callback handling should validate the expected authorization state and PKCE values before accepting an authorization response.

Unexpected or malformed OAuth callbacks should be rejected.

## Sensitive Information

Please do not commit or disclose:

* Roblox OAuth client secrets
* Access tokens
* Refresh tokens
* Authorization codes
* API keys
* Private keys
* Production credentials
* Personal authentication information
* `.env` files containing secrets

Secrets belonging to the Stew API must remain on the server and must not be embedded in the Android application.

## Supported Versions

The latest released version of Stew Android is the primary supported version.

Security fixes may not be provided for outdated or unsupported versions.

## Scope

This policy applies to the Stew Android application and its source code.

The Stew API has a separate security policy covering server-side vulnerabilities.

Third-party services, including Roblox, Android, Google Play, and hosting providers, are outside the direct scope of this policy. Vulnerabilities originating entirely within those services should be reported to their respective providers.

## Safe Testing

Security research should avoid:

* Accessing other users' accounts or data
* Using another user's OAuth credentials
* Disrupting Stew or Roblox services
* Denial-of-service testing against production services
* Attempting to compromise other users' devices
* Modifying or destroying production resources

Use a local development environment and test accounts whenever possible.

## Malicious or Modified Builds

Security reports involving unofficial, modified, or redistributed versions of Stew are welcome when they demonstrate a vulnerability in the official Stew application or infrastructure.

However, unofficial modifications are not necessarily representative of the security of an official Stew release.

## Acknowledgements

We appreciate responsible security researchers and contributors who help improve the security of Stew.
