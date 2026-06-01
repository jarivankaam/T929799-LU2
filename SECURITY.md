# Security Policy

## Supported Versions

The following versions of this project are currently receiving security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.x.x   | :white_check_mark: |
| < 1.0   | :x:                |

> Versions marked as unsupported will not receive patches. Please upgrade to a supported version.

---

## Reporting a Vulnerability

We take security issues seriously, especially given the sensitive nature of medical data handled by OpenMRS-based systems.

**Please do not report security vulnerabilities through public GitHub issues.**

Instead, report them via one of the following:

- **Email:** jjj.vankaam@student.avans.nl
- **GitHub Private Advisory:** [Report a vulnerability](../../security/advisories/new)

### What to Include

To help us triage and resolve the issue quickly, please provide:

- A clear description of the vulnerability
- Steps to reproduce (or a proof-of-concept)
- Affected version(s)
- Potential impact (e.g. data exposure, authentication bypass)
- Any suggested mitigations, if known

### What to Expect

| Stage                        | Timeframe         |
| ---------------------------- | ----------------- |
| Acknowledgement of report    | Within 48 hours   |
| Initial assessment           | Within 5 days     |
| Status update                | Every 7 days      |
| Patch release (if accepted)  | Within 30–90 days |

If the vulnerability is **accepted**, we will:
- Work with you on a fix and coordinate disclosure
- Credit you in the release notes (unless you prefer anonymity)
- Issue a CVE if applicable

If the vulnerability is **declined**, we will provide a clear explanation of why it was not considered a security risk in this context.

---

## Security Considerations for OpenMRS

This project handles protected health information (PHI). The following areas are of particular concern:

- **Authentication & session management** — OpenMRS user roles and privileges
- **Patient data access control** — ensure proper use of OpenMRS's data filtering APIs
- **Audit logging** — all access to patient records should be logged
- **Third-party modules** — only use modules from trusted, maintained sources
- **Java dependencies** — keep all dependencies up to date; we use [Dependabot](https://docs.github.com/en/code-security/dependabot) / [OWASP Dependency-Check](https://owasp.org/www-project-dependency-check/)

---

## Disclosure Policy

We follow a **coordinated disclosure** model. We ask that you give us reasonable time to patch before publicly disclosing any vulnerability. We aim to have critical issues resolved before or alongside any public disclosure.

---

## Security Best Practices for Contributors

- Never commit credentials, API keys, or patient data to the repository
- Follow the [OWASP Top 10](https://owasp.org/www-project-top-ten/) when writing new features
- Validate and sanitise all user inputs
- Use parameterised queries — never concatenate SQL strings
- Run `mvn verify` including the OWASP dependency check before opening a PR
