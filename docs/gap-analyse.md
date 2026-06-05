# Gap-analyse — NEN-7510:2024-2
**Module:** `openmrs-module-webservices.rest`  
**Datum:** 2026-06-01  

---

## A.8.3 — Toegangsbeveiliging

**Status: Gedeeltelijk**

| Bevinding | Bewijs |
|-----------|--------|
| Aanwezig: IP-filtering | `AuthorizationFilter.java` r.70: `RestUtil.isIpAllowed()` — geeft HTTP 403 bij onbekend IP |
| Aanwezig: Privilege-checks op admin en module-acties | `AdminSection.java` r.50: `Context.hasPrivilege(PRIV_MANAGE_RESTWS)` |
| Afwezig: Geen uniforme toegangscontrole per endpoint | Geen `@RequiredPrivilege`-annotaties aangetroffen op REST-resources; autorisatie wordt volledig doorgedelegeerd aan OpenMRS Core |

---

## A.8.5 — Authenticatie

**Status: Gedeeltelijk**

| Bevinding | Bewijs |
|-----------|--------|
| Aanwezig: HTTP Basic Auth ondersteund | `AuthorizationFilter.java` r.88: Base64-decodering → `Context.authenticate()` |
| Aanwezig: Sessie-invalidatie bij uitloggen | `SessionController1_9.java` r.130: `Context.logout()` + `session.invalidate()` |
| Afwezig: HTTPS wordt niet afgedwongen | Geen `requireSecure` of vergelijkbare controle gevonden; credentials reizen als Base64 (niet versleuteld) |
| Afwezig: Geen brute-force-beveiliging | Geen rate limiting of lockout na herhaalde mislukte pogingen |
| Afwezig: Geen MFA | Multi-factor authenticatie niet aangetroffen |

---

## A.8.15 — Logging

**Status: Grotendeels afwezig**

| Bevinding | Bewijs |
|-----------|--------|
| Gedeeltelijk: Succesvolle login gelogd op DEBUG | `AuthorizationFilter.java` r.104: `log.debug("authenticated [{}]", userAndPass[0])` — onzichtbaar in productie |
| Gedeeltelijk: Mislukte login gelogd op DEBUG | `AuthorizationFilter.java` r.107: `log.debug("authentication exception", ex)` |
| Afwezig: Geen logregel bij HTTP 403/401 | IP-weigering stuurt `sendError()` zonder enige `log.warn()` |
| Afwezig: Geen audit-trail | Geen `AuditLog`- of `AccessLog`-klassen aangetroffen in de module |

---

## Samenvatting

| Control | Status |
|---------|--------|
| A.8.3 Toegangsbeveiliging | Gedeeltelijk |
| A.8.5 Authenticatie | Gedeeltelijk |
| A.8.15 Logging | Grotendeels afwezig |
