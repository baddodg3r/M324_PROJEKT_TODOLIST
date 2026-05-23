# M324 ToDo-Liste

ToDo-Anwendung mit React/Vite-Frontend und Spring-Boot-Backend.

Das Frontend läuft lokal auf Port `5173` und spricht das Backend unter `http://localhost:8080` an. Das Backend stellt eine einfache REST-API bereit und speichert Tasks als JSON-Datei.

## Projektstruktur

```text
M324_PROJEKT_TODOLIST/
├── .github/workflows/pr-build.yml   GitHub Actions Build für Frontend und Backend
├── backend/                         Spring-Boot-Backend
│   ├── src/main/java/...             Controller, DTOs, Model und Services
│   ├── src/test/java/...             Backend-Tests
│   ├── data/tasks.json               lokale Task-Daten bei Start aus backend/
│   └── pom.xml                       Maven-Projekt, Java 21
├── frontend/                        React/Vite-Frontend
│   ├── src/App.jsx                   Hauptkomponente
│   ├── test/                         Frontend-Tests
│   └── package.json                  npm-Skripte
└── PROJEKT_DOKUMENTATION.md          technische Projektdokumentation
```

## Voraussetzungen

- Java 21
- Maven
- Node.js 22 oder kompatibel
- npm

Der Maven Wrapper liegt noch im Backend, wird im GitHub Actions Workflow aber nicht mehr verwendet. Der CI-Build nutzt das auf dem GitHub-Runner installierte Maven, weil der Wrapper-Download von Maven `3.8.6` mit HTTP 403 fehlschlagen kann.

## Backend starten

```sh
cd backend
mvn spring-boot:run
```

Das Backend startet auf:

```text
http://localhost:8080
```

## Frontend starten

```sh
cd frontend
npm install
npm run dev
```

Das Frontend ist danach normalerweise erreichbar unter:

```text
http://localhost:5173
```

## Tests und Builds

Frontend:

```sh
cd frontend
npm test
npm run build
```

Backend:

```sh
cd backend
mvn test
mvn -B clean package
```

Der Backend-Build erzeugt aktuell eine ausführbare Spring-Boot-JAR. In `backend/pom.xml` ist kein `<packaging>war</packaging>` gesetzt, daher verwendet Maven das Standard-Packaging `jar`. Das passt zur aktuellen Anwendung, weil Spring Boot mit eingebettetem Tomcat direkt gestartet werden kann. Eine WAR-Datei wäre nur nötig, wenn das Backend in einen externen Servlet-Container wie Tomcat deployt werden soll.

## REST-API

```text
GET  /
POST /tasks
POST /update
POST /delete
```

Die API verwendet weiterhin das JSON-Feld `taskdescription`.

Beispiel zum Erstellen:

```json
{
  "taskdescription": "Dokumentation aktualisieren"
}
```

Beispiel zum Bearbeiten:

```json
{
  "oldTaskdescription": "Alte Beschreibung",
  "taskdescription": "Neue Beschreibung"
}
```

## Speicherung

Tasks werden beim Start aus einer JSON-Datei geladen und nach Erstellen, Bearbeiten oder Löschen wieder gespeichert.

Der Speicherpfad ist im Backend relativ zum aktuellen Arbeitsverzeichnis definiert:

```text
data/tasks.json
```

Wenn das Backend mit `cd backend && mvn spring-boot:run` gestartet wird, verwendet es also:

```text
backend/data/tasks.json
```

## GitHub Actions Pipeline

Für das Projekt wurde eine CI-Pipeline mit GitHub Actions erstellt. Die Pipeline baut automatisch das React/Vite-Frontend sowie das Spring-Boot-Backend. Dadurch kann geprüft werden, ob die Software weiterhin erfolgreich gebaut werden kann.

Die Workflow-Datei befindet sich unter:

```text
.github/workflows/pr-build.yml
```

Die Pipeline wird automatisch bei Pushes auf den `main`-Branch gestartet. Zusätzlich kann sie manuell über `workflow_dispatch` ausgeführt werden.

Der Checkout-Step benötigt Leserechte auf den Repository-Inhalt. Deshalb ist im Workflow explizit gesetzt:

```yaml
permissions:
  contents: read
```

### Aufbau der Pipeline

| Job | Aufgabe |
| --- | --- |
| `frontend-build` | Baut das React/Vite-Frontend |
| `backend-build` | Baut das Spring-Boot-Backend |

Die Trennung verbessert die Übersichtlichkeit und erleichtert die Fehlersuche.

### Frontend-Build

Für das Frontend wird Node.js 22 verwendet. Der Workflow führt im Verzeichnis `frontend/` folgende Befehle aus:

```sh
npm ci
npm run build
```

`npm ci` eignet sich besser für CI/CD-Pipelines als `npm install`, weil exakt die Versionen aus `package-lock.json` verwendet werden, reproduzierbare Builds entstehen und die Installation schneller sowie stabiler abläuft.

Nach erfolgreichem Build wird der erzeugte `dist`-Ordner als GitHub Actions Artefakt `frontend-dist` hochgeladen.

### Backend-Build

Das Backend verwendet Java 21 und Maven. Der Workflow führt im Verzeichnis `backend/` folgenden Befehl aus:

```sh
mvn -B clean package
```

Dabei wird eine ausführbare Spring-Boot-JAR-Datei erzeugt und als Artefakt `backend-jar` hochgeladen.

Im Projekt ist zwar noch der Maven Wrapper vorhanden, im Workflow wird jedoch das bereits installierte Maven des GitHub-Runners verwendet. Der Grund dafür ist, dass der Maven Wrapper beim automatischen Download von Maven `3.8.6` mit HTTP-403-Fehlern fehlschlagen kann. Durch die direkte Nutzung von Maven im Runner ist der Build stabiler.

### Feste Versionen

Im Workflow wurden feste Versionen definiert:

```yaml
runs-on: ubuntu-24.04
node-version: 22
java-version: 21
```

Dies wurde bewusst gewählt, damit die Build-Umgebung stabil bleibt, keine unerwarteten Änderungen durch neue `latest`-Versionen entstehen und reproduzierbare Builds möglich sind.

`ubuntu-latest` kann sich mit der Zeit automatisch ändern. Dadurch könnten Builds plötzlich fehlschlagen, obwohl am Projekt selbst nichts geändert wurde. Mit `ubuntu-24.04` bleibt die Umgebung stabil und nachvollziehbar.

### Caching

Für npm und Maven wurde Caching aktiviert:

- Frontend: `cache: npm`
- Backend: `cache: maven`

Dadurch müssen Abhängigkeiten nicht bei jedem Build neu heruntergeladen werden und spätere Pipeline-Läufe werden schneller.

### Artefakte

Nach erfolgreichem Build werden Artefakte hochgeladen.

| Artefakt | Inhalt |
| --- | --- |
| `frontend-dist` | gebautes Frontend aus `frontend/dist` |
| `backend-jar` | ausführbare Spring-Boot-JAR aus `backend/target/*.jar` |

Die Artefakte können direkt im jeweiligen GitHub Actions Lauf im Bereich `Artifacts` als ZIP-Dateien heruntergeladen werden.

Screenshots des erfolgreichen Pipeline-Laufs und der Artefakte werden nicht im Repository gespeichert, sondern im Teams-Ordner abgelegt.
