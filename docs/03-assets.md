# Asset-identificatie & CIA/BIV-Matrix
**Project:** openmrs-module-webservices.rest  
**Sprint:** 2  
**Status:** Voltooid

Dit document identificeert de belangrijkste "kroonjuwelen" (assets) die worden verwerkt door of binnen het ontwikkelproces van de `openmrs-module-webservices.rest` module. Per asset is de kritikaliteit bepaald op basis van de CIA-triad (Vertrouwelijkheid, Integriteit, Beschikbaarheid) met een schaal van **L** (Laag), **M** (Medium) tot **H** (Hoog).

---

## 1. Risicobereidheid & Grenswaarden

Binnen dit project hanteren wij een **Zero Tolerance** beleid (risicobereidheid = 0) voor de categorieën **Vertrouwelijkheid (C)** en **Integriteit (I)** zodra deze betrekking hebben op patiëntgegevens of authenticatiemechanismen. Dit is direct afgeleid van de wettelijke verplichtingen binnen de Nederlandse gezondheidszorg (**NEN-7510:2024**) en de **AVG/GDPR**. Elk geïdentificeerd risico dat de integriteit of vertrouwelijkheid van deze High-kritieke assets bedreigt, vereist directe mitigatie in de security backlog.

---

## 2. CIA / BIV Classificatiematrix

| Gevoelig Gegeven / Asset | Beschrijving van de Asset | Vertrouwelijkheid (C) | Integriteit (I) | Beschikbaarheid (A) | Technische Referentie / Context |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **Patiënt- & Medische Data** | NAW-gegevens, diagnoses, medische observaties en medicatievoorschriften die via de REST endpoints worden ontsloten. | **H** | **H** | **M** | `PatientResource1_8.java`, `ObservationResource1_8.java` via `/ws/rest/v1/patient` |
| **Authenticatie Credentials** | Base64-encoded strings van gebruikersnamen en wachtwoorden die worden meegezonden in de HTTP-headers. | **H** | **H** | **L** | `AuthorizationFilter.java` r.88 (`Context.authenticate()`) |
| **Audit & Access Logs** | Het loggen en vastleggen van transacties (wie bekijkt of wijzigt welke medische resource via de API). | **M** | **H** | **M** | `AuthorizationFilter.java` r.104 (`log.debug()`) |
| **Sessie Tokens** | Actieve gebruikerssessies op de server die toegang verlenen tot de REST-resources zonder herauthenticatie. | **H** | **M** | **L** | `SessionController1_9.java` r.130 (`session.invalidate()`) |
| **API Documentatie (Swagger)** | De formele specificatie van blootgestelde endpoints, parameters en datastructuren (`apiDocs.htm`). | **L** | **H** | **M** | `openmrs-contrib-apidocs` repository / Swagger UI integratie. |
| **CI/CD Pipeline Secrets** | Tokens en credentials (`SONAR_TOKEN`) waarmee de GitHub Actions pipeline mag communiceren met externe scanners. | **H** | **H** | **L** | Geheimhouding via GitHub Repository Secrets in `.github/workflows/sonar.yml`. |
| **Systeeminformatie & Metadata** | Serverversies (Java, OpenMRS core) en database-lay-outs die onbedoeld kunnen lekken via foutmeldingen. | **M** | **L** | **L** | Gedrag van Java Exception Stacktraces in de REST-controllers. |

---

## 3. Toelichting op de Classificatie

* **Patiëntdata (C:H, I:H):** Onbevoegde inzage (C) leidt direct tot een ernstig datalek onder de AVG. Manipulatie van data (I) kan resulteren in incorrecte medische dossiers, wat levensgevaarlijk is voor de patiëntveiligheid.
* **Audit Logs (I:H):** De integriteit van logs is cruciaal voor **NEN-7510 A.8.15**. Als een aanvaller logs kan aanpassen of wissen, kan er geen betrouwbare forensische audit plaatsvinden na een incident.
* **API Documentatie (I:H):** Als een aanvaller de documentatie manipuleert, kunnen legitieme applicaties misleid worden om medische gegevens naar kwaadaardige of onbeveiligde endpoints te sturen.
* **Pipeline Secrets (C:H, I:H):** Diefstal van deze tokens stelt een aanvaller in staat om de geautomatiseerde kwaliteitscontroles (Quality Gates) te omzeilen of kwaadaardige code te injecteren in het build-proces.