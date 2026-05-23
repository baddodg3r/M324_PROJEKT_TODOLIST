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

## GitHub Actions

Der Workflow [pr-build.yml](.github/workflows/pr-build.yml) baut Frontend und Backend getrennt:

- Frontend: Node.js 22, `npm ci`, `npm run build`
- Backend: Java 21, `mvn -B clean package`

Nach erfolgreichem Lauf werden Build-Artefakte hochgeladen:

- `frontend-dist`
- `backend-jar` mit der erzeugten Spring-Boot-JAR aus `backend/target/*.jar`

Diese liegen im GitHub Actions Lauf unter `Artifacts` als ZIP-Download bereit.
