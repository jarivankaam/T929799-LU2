# Developer Onboarding — T929799-LU2

Welcome to the project. This guide gets a new developer from zero to a working local setup.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Prerequisites](#2-prerequisites)
3. [Cloning and Building](#3-cloning-and-building)
4. [Project Structure](#4-project-structure)
5. [Running the Tests](#5-running-the-tests)
6. [Code Quality — SonarCloud](#6-code-quality--sonarcloud)
7. [Branching and Workflow](#7-branching-and-workflow)
8. [Security Guidelines](#8-security-guidelines)
9. [Contacts](#9-contacts)

---

## 1. Project Overview

This repository is a fork of the [OpenMRS REST Web Services Module](https://github.com/openmrs/openmrs-module-webservices.rest), maintained as part of the Avans University course **T929799-LU2**.

The module exposes an OpenMRS instance as a REST API. Other applications can query and update patient records, encounters, and other clinical data through HTTP endpoints.

**Key technologies:**

| Technology | Purpose |
|---|---|
| Java 8 | Application language |
| Maven 3.x | Build and dependency management |
| OpenMRS Core | Underlying platform |
| JUnit + Rest-Assured | Integration testing |
| SonarCloud | Static analysis and code quality |
| GitHub Actions | CI pipeline |
| OWASP Dependency-Check | Security vulnerability scanning |

---

## 2. Prerequisites

Install the following tools before you begin.

### Java 8

The project requires exactly **Java 8** (OpenJDK or Oracle JDK).

```bash
# Verify your Java version
java -version
# Should output: openjdk version "1.8.x_xxx" or similar
```

If you have multiple JDK versions, set `JAVA_HOME` to point at Java 8:

```bash
export JAVA_HOME=/path/to/jdk8
export PATH=$JAVA_HOME/bin:$PATH
```

### Maven 3.x

```bash
# Verify Maven is installed
mvn -version
# Should output: Apache Maven 3.x.x
```

Download from [maven.apache.org](https://maven.apache.org/download.cgi) if not installed.

### Git

```bash
git --version
```

---

## 3. Cloning and Building

```bash
# Clone the repository
git clone <this-repo-url>
cd T929799-LU2

# Full build (compiles, runs unit tests, packages the .omod)
mvn clean install

# Build without running tests (faster for the first time)
mvn clean install -DskipTests
```

A successful build produces `.omod` files inside `omod/target/` and `omod-common/target/`. These are the deployable module files for an OpenMRS server.

### Importing into an IDE

**IntelliJ IDEA (recommended):**
1. File → Open → select the root `pom.xml`
2. Choose "Open as Project"
3. Wait for Maven to index dependencies

**Eclipse:**
1. File → Import → Existing Maven Projects
2. Select the root directory
3. Import all detected modules

The `OpenMRSFormatter.xml` file in the root is the project's code formatter. Import it in your IDE:
- IntelliJ: Settings → Editor → Code Style → Java → Import Scheme → IntelliJ IDEA code style XML
- Eclipse: Window → Preferences → Java → Code Style → Formatter → Import

---

## 4. Project Structure

```
T929799-LU2/
├── omod-common/          # Shared code used by both omod and integration tests
├── omod/                 # The actual OpenMRS module (.omod output)
├── integration-tests/    # JUnit + Rest-Assured integration test suite
├── bamboo-specs/         # CI pipeline specs (Bamboo)
├── .github/workflows/    # GitHub Actions workflows (SonarCloud)
├── pom.xml               # Root Maven POM (groupId: org.openmrs.module, version: 3.2.0)
├── SECURITY.md           # Security policy and responsible disclosure
└── OpenMRSFormatter.xml  # Code style formatter
```

**Module dependency flow:**

```
omod-common  →  omod
     ↓
integration-tests (depends on a running OpenMRS server)
```

---

## 5. Running the Tests

### Unit Tests

Unit tests run automatically as part of `mvn clean install`. To run them in isolation:

```bash
mvn test
```

### Integration Tests

Integration tests require a running OpenMRS instance with the module installed.

1. Start your local OpenMRS server (default: `http://localhost:8080/openmrs`)
2. Deploy the `.omod` built in the previous step
3. Run the tests:

```bash
mvn clean verify -Pintegration-tests -DtestUrl=http://admin:Admin123@localhost:8080/openmrs
```

Replace `admin:Admin123` with your local OpenMRS credentials if different.

---

## 6. Code Quality — SonarCloud

This project uses [SonarCloud](https://sonarcloud.io) for static analysis. The analysis runs automatically on every push to `Production` via the GitHub Actions workflow in `.github/workflows/sonarqube.yml`.

**To view results:** go to SonarCloud and look up the `jarivankaam` organisation.

**Before opening a pull request, make sure:**
- No new blocker or critical issues are introduced
- Test coverage does not drop significantly
- Run the OWASP dependency check locally:

```bash
mvn verify -Powasp-dependency-check
```

---

## 7. Branching and Workflow

| Branch | Purpose |
|---|---|
| `Production` | Main branch — all PRs target this branch |

**Standard contribution flow:**

```bash
# 1. Create a feature branch from Production
git checkout Production
git pull origin Production
git checkout -b feature/your-feature-name

# 2. Make your changes and commit
git add <files>
git commit -m "Short description of what and why"

# 3. Push and open a pull request
git push origin feature/your-feature-name
```

**PR checklist:**
- [ ] `mvn clean install` passes locally
- [ ] New code has unit tests where applicable
- [ ] No hardcoded credentials or patient data
- [ ] SonarCloud analysis passes (no new blockers)
- [ ] Integration tests pass if REST endpoints were changed

---

## 8. Security Guidelines

This project handles **protected health information (PHI)**. Follow these rules without exception:

- Never commit credentials, API keys, tokens, or patient data to the repository
- Validate and sanitise all user inputs
- Use parameterised queries — never concatenate SQL strings
- Follow the [OWASP Top 10](https://owasp.org/www-project-top-ten/) when writing new features
- Run `mvn verify` including the OWASP dependency check before opening a PR

To report a security vulnerability, see [SECURITY.md](SECURITY.md). **Do not open a public GitHub issue for security bugs.**

---

## 9. Contacts

| Role | Contact |
|---|---|
| Project maintainer | jjj.vankaam@student.avans.nl |
| Security reports | jjj.vankaam@student.avans.nl or via GitHub Private Advisory |
