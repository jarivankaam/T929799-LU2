# Mini-complianceverslag: Pipeline Compliance
**Project:** openmrs-module-webservices.rest  
**Sprint / Fase:** Sprint 1 Oplevering (Gecorrigeerd op basis van Gap-analyse)  
**Systeemeigenaar:** Team OpenMRS LU2  
**Status:** Voltooid / Gereed voor Eindcheck

---

## 1. Inleiding & Doelstelling
Dit complianceverslag dient als formeel bewijsmateriaal voor de audit-trail binnen de CI/CD-pipeline (GitHub Actions). Het doel is om aan te tonen hoe de geautomatiseerde pipeline de kwetsbaarheden en ontbrekende security-controls opvangt die zijn geïdentificeerd tijdens de nulmeting (`01-gap-analyse.md`).

Omdat deze module via `AuthorizationFilter.java` en REST-endpoints direct toegang verleent tot medische persoonsgegevens, dwingt de pipeline harde acceptatiecriteria (Quality Gates) af om te voorkomen dat code met security-regressie naar de productieomgeving wordt gepusht.

---

## 2. Compliance & Kwaliteitsmatrix

| NEN-7510 Control | Gedetecteerd Gat (Uit Gap-analyse) | Geautomatiseerde Pipeline-maatregel | Gekoppeld Acceptatiecriterium (Non-Functionals) | Locatie van het Bewijs / Audit Trail |
| :--- | :--- | :--- | :--- | :--- |
| **A.8.3** <br>*(Toegangsbeveiliging)* | Autorisatie wordt doorgedelegeerd naar OpenMRS Core; er is geen uniforme controle of actieve handhaving per endpoint binnen de REST-resources zelf. | **SonarQube & CodeQL SAST Scanning:** <br>De pipeline scant Java-controllers op het ontbreken van expliciete privilege-checks en controleert of endpoints onbedoeld openbaar (anoniem) toegankelijk zijn gemaakt. | De pipeline breekt de build af indien SonarQube of CodeQL een kwetsbaarheid vindt met severity 'High' of 'Critical' (bijv. CWE-284: Improper Access Control). | **GitHub Actions Run:** <br>Stap: `Build and analyze`<br>Sonar Project Key: `jarivankaam_T929799-LU2` |
| **A.8.5** <br>*(Authenticatie)* | HTTP Basic Auth transporteert onversleutelde Base64-credentials (`AuthorizationFilter.java` r.88) zonder dat HTTPS wordt afgedwongen of brute-forcebeveiliging aanwezig is. | **CodeQL Cryptography & Configuration Audit:** <br>Scant op het gebruik van onveilige/onversleutelde transportprotocollen en hardcoded credentials (Secret Scanning). Tevens controleert het of configuratiefouten in de controllers blootliggen. | *1.* 0 actieve Secret Scanning alerts op de branch.<br>*2.* CodeQL mag geen waarschuwingen genereren op het onveilig transporteren van gevoelige data (CWE-319). | **GitHub Repository:** <br>Tabblad *Security -> Secret scanning alerts* & *Code scanning alerts*. |
| **A.8.15** <br>*(Logging)* | Audit-trail is grotendeels afwezig. Logregels voor (mislukte) authenticatie staan op `DEBUG` (`AuthorizationFilter.java` r.104/107) en zijn onzichtbaar in productie. IP-weigeringen genereren geen logwaarschuwing. | **SonarQube Code Smell & Maintainability Analysis:** <br>De pipeline controleert Java-code specifiek op misconfiguraties in logging, zoals lege catch-blocks die security-exceptions onderdrukken en het onjuist gebruik van log-niveaus (bijv. security-events op DEBUG i.p.v. WARN/INFO). | De code moet voldoen aan de 'Quality Gate' van SonarQube. Nieuwe code mag de 'Maintainability Rating A' niet verslechteren en mag geen kritieke code smells introduceren rondom foutafhandeling. | **SonarQube Dashboard:** <br>Kwaliteitsrapportage gekoppeld aan de meest recente commit op de `Production` branch via de Maven Sonar-plugin. |
| **A.12.6.1** <br>*(Kwetsbaarheden)* | Risico op bekende kwetsbaarheden in verouderde 3rd party Java-bibliotheken (SCA) binnen de OpenMRS legacy architectuur. | **Software Bill of Materials (SBOM) Generatie:** <br>Bij elke succesvolle build op de productie-branch genereert de pipeline automatisch een up-to-date CycloneDX-inventarisatie van de complete Maven-dependency-tree. | Succesvolle extraction en publicatie van het `bom.xml` bestand als een gecertificeerd build-artifact. | **GitHub Actions Artifacts:** <br>Gegenereerd bestand: `target/bom.xml` (90 dagen retentie via `upload-artifact@v4`). |

---

## 3. Technische Handhaving & Acceptatiecriteria

De pipeline-maatregelen zijn via **Branch Protection Rules** gecodificeerd als harde technische poortwachters voor de `Production` branch:

1. **Vier-ogen principe (NEN-7510 A.14.2.1):** Direct pushen naar de hoofdbranches is geblokkeerd. Wijzigingen in kritieke bestanden zoals `AuthorizationFilter.java` moeten via een Pull Request lopen en vereisen minimaal één goedgekeurde peer-review.
2. **Required Status Checks:** De GitHub Actions-job `Build and analyze` (`mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar`) is ingesteld als verplicht. Een falende testsuite of een 'Failed Quality Gate' vanuit SonarQube maakt de merge-knop fysiek onbruikbaar.

---

## 4. Evaluatie t.o.v. Sprint 1 Checklist

* [x] **GitHub repository heeft branch protection en Dependabot actief:** Gerealiseerd. Branch protection blokkeert commits die niet door de security-checks komen.
* [x] **SBOM-bestand wordt als CI-artifact aangemaakt in Actions:** Volledig operationeel via de gekoppelde `cyclonedx-maven-plugin`.
* [x] **Gap-analyse dekt minimaal 3 NEN-7510 controls met bewijs:** Succesvol voltooid in `01-gap-analyse.md` met directe herleiding naar de codebase van de module.

> **Verbeterpunt voor Sprint 2:** Zoals vastgesteld in de gap-analyse ontbreekt momenteel een actieve dependency-scan (SCA) omdat `package-ecosystem: ""` in `dependabot.yml` leeg is. Dit wordt bij de start van Sprint 2 gecorrigeerd naar `"maven"` om kwetsbaarheden in de gebruikte bibliotheken proactief te blokkeren.