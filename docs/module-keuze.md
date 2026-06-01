# Documentatie Modulekeuze: OpenMRS Web Services REST

## 1. Algemene Gegevens van de Module

* **Modulenaam:** OpenMRS Web Services REST Module
* **Gekozen Versie:** v2.x / Legacy-versie 
* **Broncode Locatie:** [OpenMRS Web Services REST GitHub Repository](https://github.com/openmrs/openmrs-module-webservices.rest)
* **Doel van de Module:** Deze module stelt de core-functionaliteit van OpenMRS (patiëntgegevens, medische dossiers, observaties, encounters) beschikbaar via een RESTful API. Het vormt de cruciale brug tussen de backend-database en moderne frontend-applicaties, mobiele apps en externe zorgsystemen.

## 2. Motivatie voor de Keuze

De keuze voor de webservices.rest-module is gemaakt op basis van drie zaken: technische complexiteit, de strategische scope binnen de architectuur en de directe impact op informatiebeveiliging in de zorg.

### 2.1 Hoge Technische Complexiteit
Tijdens onze verkenning viel de hoge mate van complexiteit direct op. Dit sluit perfect aan bij de eisen van LU 2:
* **Complexe Object-Mapping:** De module moet complexe, diep geneste Java-domeinmodellen van OpenMRS dynamisch converteren naar JSON/XML-representaties en vice versa.
* **Uitgebreide Reflectie en Subclassing:** Er wordt intensief gebruikgemaakt van custom serializers, deserializers en reflectie, wat de leesbaarheid en onderhoudbaarheid bemoeilijkt. Dit biedt een uitstekend fundament voor een gestructureerd software-assessment op onderhoudbaarheid, refactoring-patronen en het bouwen van een Proof of Concept (PoC).

### 2.2 Strategische Scope en het Aanvalsoppervlak (Attack Surface)
De REST-module fungeert als de primaire ingang van het OpenMRS-ecosysteem. 
* **Inkomende Laag van Aanvallen:** Vrijwel alle externe interacties met het elektronisch patiëntendossier passeren deze module. In security-architecturen is dit het meest kritieke component: als deze laag valt, ligt het gehele achterliggende systeem open.
* **Concentratie van Security-Mechanismen:** Doordat alle verzoeken hier binnenkomen, komen de belangrijkste security-vulnerabilities in deze module naar voren. Dit maakt het een uiterst relevant en leerzaam object voor security code reviews en penetratietesten.

### 2.3 Relevantie voor NEN-7510 Compliance & Wetgeving
Binnen de Nederlandse gezondheidszorg is naleving van de **NEN-7510:2024** norm wettelijk verplicht. De webservices.rest-module is bij goed geschikt om te toetsen aan deze norm, omdat de volgende kernaspecten hierin samenkomen en geconcentreerd zijn:
1.  **Authenticatie & Autorisatie (NEN-7510 Control 9 - Toegangsbeveiliging):** De REST-endpoints bepalen wie toegang krijgt en welke specifieke resources een gebruiker mag inzien of wijzigen. Een zwakte hierin leidt direct tot datalekken.
2.  **Data-Exposure (NEN-7510 Control 12 - Beveiliging van de bedrijfsvoering / Cryptografie)**
3.  **Toegangscontrole & Logging (NEN-7510 Control 12.4 - Logging en monitoring)**

## 3. Verwachte Aanpak binnen de SDLC

Voor nu verwachten wij dat we ons aan de volgende stappen te houden binnen de SDLC van LU2:

```
[ Software Assessment ] ---> [ SBOM & CVE Analyse ] ---> [ Penetration Testing (Pre-fix) ]
          |                           |                             |
          v                           v                             v
[ Refactoring & Design ]    [ Library Updates Advies ]    [ Mitigatie & Code Fixes ]
          |                           |                             |
          +---------------------------+-----------------------------+
                                      |
                                      v
                        [ PoC Herassessment & Verdediging ]
```

* **Onderhoudbaarheid:** Uitvoeren van een code-assessment (met o.a. statistische analyse/AI-tooling) om de legacy architectuur van de REST-controllers te herontwerpen met bewezen ontwerppatronen, zonder regressie te veroorzaken.
* **Security & Compliance:** Het uitvoeren van een gerichte penetratietest op de REST-endpoints, het in kaart brengen van 3rd party bibliotheken (SBOM), en het bouwen van een PoC die de effectiviteit van onze security-mitigaties zwart-op-wit aantoont via een succesvolle re-test.
