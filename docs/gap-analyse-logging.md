# Gap-analyse — NEN-7510:2024-2
**Module:** openmrs-module-webservices.rest  
**Datum:** 2026-06-12  
**Scope:** Logging & Gebeurtenisregistratie  
**Geanalyseerde bronbestanden:** AuthorizationFilter.java · SessionController1_9.java · BaseRestController.java · Activator.java

---

## A.8.15 — Gebeurtenisregistratie (Logging)
**Status: Grotendeels afwezig**

| Bevinding | Bewijs |
|-----------|--------|
| **Aanwezig:** Succesvolle login gelogd | `AuthorizationFilter.java` r.108: `log.debug("authenticated [{}]", userAndPass[0])` |
| **Aanwezig:** Mislukte login gelogd | `AuthorizationFilter.java` r.113: `log.debug("authentication exception", ex)` |
| **Aanwezig:** 500-errors gelogd op ERROR | `BaseRestController.java` r.124: `log.error(ex.getMessage(), ex)` |
| **Aanwezig:** 4xx-errors gelogd op INFO | `BaseRestController.java` r.127: `log.info(ex.getMessage(), ex)` alleen niet-auth fouten |
| **Aanwezig:** Module opstarten/stoppen | `Activator.java` r.31/36: `log.info("Started/Stopped the REST Web Service module")` |
| **Kritiek afwezig:** Login-events op DEBUG — onzichtbaar in productie | `AuthorizationFilter.java` r.108/113: `log.debug()` productie-loglevel is standaard INFO of hoger; geslaagde én mislukte inlogpogingen worden in productie **nooit** geschreven |
| **Afwezig:** Uitloggen niet gelogd | `SessionController1_9.java` r.117–122: `Context.logout()` en `session.invalidate()` worden aangeroepen zonder enig log-statement |
| **Afwezig:** 401 Unauthorized niet gelogd | `BaseRestController.java` r.89–101: `apiAuthenticationExceptionHandler()` stuurt 401-response maar bevat geen `log.*` aanroep |
| **Afwezig:** 403 Forbidden niet gelogd | `BaseRestController.java` r.89–101: `apiAuthenticationExceptionHandler()` stuurt 403-response maar bevat geen `log.*` aanroep; herhaalde ongeautoriseerde toegangspogingen zijn volledig onzichtbaar |
| **Afwezig:** IP-blokkering niet gelogd | `AuthorizationFilter.java` r.68: `httpresponse.sendError(SC_FORBIDDEN, ...)` geblokkeerd IP-adres wordt in response gemeld maar niet in logbestand vastgelegd |
| **Afwezig:** Sessietime-out niet gelogd | `AuthorizationFilter.java` r.73: `httpResponse.sendError(SC_UNAUTHORIZED, "Session timed out")` zonder log-statement |
| **Kritiek afwezig:** Toegang tot /session/diag niet gelogd | `SessionController1_9.java` r.164: commentaar in code luidt expliciet *"No authorization check, accessible to any caller (authenticated or not)"*; endpoint retourneert gebruikersnaam, rollen en privileges zonder autorisatie én zonder logging |
| **Afwezig:** Rate limit overschrijding niet gelogd | Geen rate limiting geïmplementeerd in de module; overschrijding kan daardoor ook niet worden gedetecteerd of gelogd (zie ook gap-analyse A.8.3) |
| **Afwezig:** Geen audit-trail | Geen `AuditLog`- of `AccessLog`-klassen aangetroffen; geen structureel logformaat met tijdstempel, gebruiker, actie en resource |
| **Gedeeltelijk:** 500-errors lekken stacktrace naar client | `BaseRestController.java` r.124: `log.error(ex.getMessage(), ex)` logt correct server-side, maar stacktrace wordt ook teruggestuurd in HTTP-response, interne klassenamen zoals `UserResource1_8` worden zo zichtbaar voor aanvallers |

---

## Overzicht per event: huidig versus gewenst

| Event | Gelogd? | Log-niveau | Gevoelige data | Compliant A.8.15? |
|-------|---------|------------|----------------|-------------------|
| Mislukte inlogpoging | Ja | DEBUG (onzichtbaar) | Nee | Non-compliant |
| Geslaagde inlogpoging | Ja | DEBUG (onzichtbaar) | Nee | Non-compliant |
| Uitloggen | Nee | — | Nee | Non-compliant |
| 403 Forbidden | Nee | — | Nee | Non-compliant |
| 401 Unauthorized | Nee | — | Nee | Non-compliant |
| IP-adres geblokkeerd | Nee | — | Nee | Non-compliant |
| Sessietime-out | Nee | — | Nee | Non-compliant |
| Toegang /session/diag | Nee | — | **Ja** | Kritiek |
| Rate limit overschrijding | Nee | — | Nee | Non-compliant |
| Interne serverfout (500) | Ja | ERROR | Ja (stacktrace) | Gedeeltelijk |
| Client-fout (4xx, niet 401/403) | Ja | INFO | Nee | Gedeeltelijk |
| Module opstarten/stoppen | Ja | INFO | Nee | Compliant |

---

## Gap: Huidig versus Gewenst

| Aspect | Huidige situatie | Gewenste situatie | Aanbevolen actie |
|--------|-----------------|-------------------|-----------------|
| Inlogpogingen | Gelogd op DEBUG, onzichtbaar in productie | Gelogd op WARN (mislukking) en INFO (succes) | Logniveau verhogen in `AuthorizationFilter.java` r.108/113 |
| Uitloggen | Niet gelogd | Elke sessiebeëindiging gelogd op INFO | `log.info()` toevoegen in `delete()` van `SessionController1_9.java` |
| Autorisatiefouten | 401/403 niet gelogd | Elke 401/403 gelogd op WARN met gebruiker en endpoint | `log.warn()` toevoegen in `apiAuthenticationExceptionHandler()` |
| IP-blokkering | sendError() zonder log | Geblokkeerd IP gelogd op WARN | `log.warn()` toevoegen in `AuthorizationFilter.java` r.68 |
| /session/diag | Geen autorisatie, geen logging | Endpoint beveiligd of verwijderd; toegang gelogd | Endpoint verwijderen of voorzien van autorisatiecheck + `log.warn()` |
| Rate limit | Geen rate limiting, geen logging | Overschrijding gelogd op WARN | Rate limiting implementeren (nginx) + log bij overschrijding |
| Stacktrace in response | Interne foutdetails zichtbaar voor client | Generieke foutmelding naar client, details alleen in server-log | HTTP-response sanitizen in `BaseRestController.java` |

---

## Samenvatting

| Control | Status |
|---------|--------|
| A.8.3 Toegangsbeveiliging | Gedeeltelijk |
| A.8.5 Authenticatie | Gedeeltelijk |
| A.8.15 Logging & Gebeurtenisregistratie | **Grotendeels afwezig** |

Van de twaalf beveiligingsrelevante events is er slechts één volledig compliant (module opstarten/stoppen). De meest kritieke is dat mislukte inlogpogingen, autorisatiefouten en toegang tot het onbeveiligde `/session/diag`-endpoint volledig onzichtbaar zijn in de productielogboeken. Een aanvaller die brute force, privilege escalation of sessie-extractie uitvoert laat geen detecteerbaar spoor achter, wat haaks staat op NEN-7510:2024-2 A.8.15.
