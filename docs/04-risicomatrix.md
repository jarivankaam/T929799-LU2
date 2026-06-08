# Risicomatrix & Threat Modelling
**Project:** openmrs-module-webservices.rest  
**Sprint:** 2  
**Status:** Voltooid

Dit document bevat de volledige risicomatrix voor de OpenMRS REST-module en het bijbehorende CI/CD-ontwikkelproces. Elk risico is direct herleidbaar naar een specifiek kroonjuweel (asset) uit de asset-identificatie conform de eisen van Sprint 2.

---

## 1. Methodologie & Risicocriteria

De risicoscore wordt berekend met de formule:  
$$\text{Risicoscore} = \text{Kans (K)} \times \text{Impact (I)}$$

Zowel **Kans** als **Impact** worden gescoord op een schaal van 1 tot 5:

### Kans-schaal (K)
* **1 (Zeer laag):** Theoretisch mogelijk, maar zeer onwaarschijnlijk in de praktijk.
* **2 (Laag):** Er is een barrière, misbruik vereist specifieke omstandigheden of privileges.
* **3 (Medium):** Mogelijk, de kwetsbaarheid is aanwezig en redelijk eenvoudig te identificeren.
* **4 (Hoog):** Waarschijnlijk, de kwetsbaarheid ligt open en er zijn actieve exploit-vectoren.
* **5 (Zeer hoog):** Vrijwel zeker dat dit misbruikt wordt binnen afzienbare tijd.

### Impact-schaal (I)
* **1 (Verwaarloosbaar):** Geen impact op bedrijfsvoering, compliance of patiëntveiligheid.
* **2 (Gering):** Minimale blootstelling van niet-gevoelige metadata of kortstondige hinder.
* **3 (Substantieel):** Impact op systeembeschikbaarheid of beperkt datalek van niet-medische aard.
* **4 (Ernstig):** Groot datalek van medische gegevens (PHI), mogelijke schending van de AVG.
* **5 (Kritiek/Catastrofaal):** Grootschalig datalek, risico voor patiëntveiligheid, zware NEN-7510/autoriteitsboetes, of volledige overname van de infrastructuur.

### Risicobereidheid & Grenswaarden
* **Score 1 - 6 (Groen):** Acceptabel risico. Geen directe actie vereist, wel periodiek monitoren.
* **Score 8 - 10 (Oranje):** Medium risico. Mitigeren binnen een afzienbare termijn (opnemen in backlog).
* **Score 12 - 25 (Rood):** Hoog / Kritiek risico. **Buiten de risicobereidheid.** Directe blokkade, mitigatie is verplicht in de huidige of eerstvolgende sprint.

---

## 2. De Uitgebreide Risicomatrix

| Risk ID | Gekoppelde Asset | Dreiging / Threat Context | Kans (K) | Impact (I) | Risicoscore | NEN-7510 Control | Status t.o.v. Grenswaarde |
| :--- | :--- | :--- | :---: | :---: | :---: | :--- | :--- |
| **R1** | Patiënt- & Medische Data | **Onbevoegde toegang tot patiëntdata (Broken Access Control)**<br>Een aanvaller omzeilt endpoint-beveiliging omdat uniforme `@RequiredPrivilege` annotaties ontbreken op REST-resources en delegatie naar core faalt of verkeerd geconfigureerd is. | 4 | 5 | **20 (Kritiek)** | **A.8.3** *(Toegangsbeveiliging)* | **Buiten bereidheid (Rood)**<br>Direct herstellen. |
| **R2** | Authenticatie Credentials | **Interceptie van medische credentials (Man-in-the-Middle)**<br>Gebruikers authenticeren via HTTP Basic Auth. Omdat HTTPS niet binnen de module wordt afgedwongen, reizen Base64-credentials onversleuteld over het (interne) netwerk. | 4 | 4 | **16 (Hoog)** | **A.8.5** *(Authenticatie)* | **Buiten bereidheid (Rood)**<br>Direct herstellen. |
| **R3** | Audit & Access Logs | **Onopgemerkt datalek / Wissen van sporen (Gebrek aan Audit Trail)**<br>Authenticatie-events loggen uitsluitend op `DEBUG`-niveau en IP-weigeringen genereren geen logwaarschuwing. Een aanvaller kan onopgemerkt data exfiltreren of sporen wissen. | 3 | 4 | **12 (Hoog)** | **A.8.15** *(Logging)* | **Buiten bereidheid (Rood)**<br>Mitigatie verplicht. |
| **R4** | CI/CD Pipeline Secrets | **Diefstal of misbruik van Pipeline Secrets (`SONAR_TOKEN`)**<br>Indien pipeline secrets uitlekken via foutieve workflow-logs of een gecompromitteerd developer-account, kan een aanvaller security Quality Gates manipuleren of malicieuze code builden. | 2 | 5 | **10 (Medium)** | **A.14.2.1** *(Veilig programmeren)* | **Grensgeval (Oranje)**<br>Inrichten op backlog. |
| **R5** | Sessie Tokens | **Session Hijacking via voorspelbare of onveilig beheerde sessies**<br>Sessie-tokens worden na een REST-aanroep niet direct ongeldig gemaakt of via onveilige cookies getransporteerd, waardoor een aanvaller een actieve sessie van een arts kan overnemen. | 3 | 3 | **9 (Medium)** | **A.8.5** *(Authenticatie)* | **Grensgeval (Oranje)**<br>Inrichten op backlog. |
| **R6** | API Documentatie (Swagger) | **Defacement of manipulatie van de API-specificatie**<br>Een aanvaller slaagt erin de gegenereerde Swagger/OpenAPI-documentatie aan te passen, waardoor legitieme developers of client-applicaties medische data naar kwaadaardige endpoints sturen. | 2 | 3 | **6 (Laag)** | **A.12.1.1** *(Bedrijfsvoering)* | **Binnen bereidheid (Groen)**<br>Periodiek controleren. |
| **R7** | Systeeminformatie & Metadata | **Information Disclosure via Java Exception Stacktraces**<br>Wanneer de REST-API crasht, genereert de controller een rauwe stacktrace naar de client. Dit lekt specifieke server-, database- en Java-versies die gebruikt worden voor gerichte exploitatie. | 3 | 2 | **6 (Laag)** | **A.12.4.1** *(Logging/Monitoring)* | **Binnen bereidheid (Groen)**<br>Periodiek controleren. |

---

## 3. Aanvullende Risico-evaluatie van het CI/CD-proces

Naast de runtime-risico's van de software zelf, identificeert het threat-modelling proces een specifiek risico binnen de release-pipeline:

* **SCA / Supply Chain Risico (Gekoppeld aan R1/R4):** De module maakt gebruik van verouderde libraries in de Maven `pom.xml`. Als een van deze diep geneste dependencies een bekende Remote Code Execution (RCE) kwetsbaarheid bevat, kan de gehele OpenMRS-server via een malicieus REST-verzoek worden overgenomen. Dit risico scoort een **Kans: 3** en **Impact: 4**, wat resulteert in een **Risicoscore van 12 (Hoog/Rood)**. De geautomatiseerde Snyk/CycloneDX-scan in Sprint 2 is ingericht om dit specifiek te blokkeren.