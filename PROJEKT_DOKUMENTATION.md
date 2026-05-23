# Projekt-Dokumentation: ToDo-Liste

Diese Dokumentation beschreibt den aktuellen Stand der ToDo-Anwendung im Repository. Sie ist gegen den Code in `frontend/`, `backend/` und `.github/workflows/pr-build.yml` abgeglichen.

## Überblick

Die Anwendung besteht aus zwei Teilen:

- `frontend/`: React 19 mit Vite 6
- `backend/`: Spring Boot 3.4.5 mit Java 21

Das Frontend zeigt eine ToDo-Liste an und sendet HTTP-Requests direkt an das Backend unter `http://localhost:8080`. Das Backend verwaltet Tasks in einer Liste und speichert diese Liste als JSON-Datei.

## Voraussetzungen

Für lokale Entwicklung:

- Java 21
- Maven
- Node.js 22 oder kompatibel
- npm

Der Backend-Build ist in `backend/pom.xml` auf Java 21 konfiguriert:

```xml
<java.version>21</java.version>
```

## Projektstruktur

```text
M324_PROJEKT_TODOLIST/
├── README.md
├── PROJEKT_DOKUMENTATION.md
├── .github/workflows/pr-build.yml
├── backend/
│   ├── pom.xml
│   ├── data/tasks.json
│   └── src/
│       ├── main/java/com/example/demo/
│       │   ├── DemoApplication.java
│       │   ├── controller/TaskController.java
│       │   ├── dto/TaskDTO.java
│       │   ├── dto/TaskUpdateDTO.java
│       │   ├── model/Task.java
│       │   └── service/
│       │       ├── TaskService.java
│       │       └── TaskStorageService.java
│       └── test/java/com/example/demo/
└── frontend/
    ├── package.json
    ├── src/App.jsx
    └── test/
```

Das alte Verzeichnis `backend/bin/` wurde entfernt. Es enthielt eine veraltete Kopie des Backend-Projekts mit alter Java-17-Konfiguration und gehoert nicht zur aktuellen Anwendung.

## Backend

### Start

```sh
cd backend
mvn spring-boot:run
```

Das Backend läuft danach unter:

```text
http://localhost:8080
```

### API

Der Controller `TaskController` stellt diese Endpoints bereit:

```text
GET  /
POST /tasks
POST /update
POST /delete
```

`GET /` gibt alle Tasks als Liste von DTOs zurück.

`POST /tasks` erstellt einen Task. Erwarteter Body:

```json
{
  "taskdescription": "Neuer Task"
}
```

`POST /update` bearbeitet einen bestehenden Task. Erwarteter Body:

```json
{
  "oldTaskdescription": "Alter Text",
  "taskdescription": "Neuer Text"
}
```

`POST /delete` löscht einen Task. Erwarteter Body:

```json
{
  "taskdescription": "Zu loeschender Task"
}
```

Leere Tasktexte werden vom Controller mit `400 Bad Request` abgelehnt.

### Fachlogik

Die Fachlogik liegt in `TaskService`.

Aktuelles Verhalten:

- Beim Start werden gespeicherte Tasks aus der JSON-Datei geladen.
- Neue Tasks werden nur hinzugefuegt, wenn noch kein Task mit derselben `taskdescription` existiert.
- Updates werden ignoriert, wenn der neue Text bereits bei einem anderen Task existiert.
- Nach Erstellen, Bearbeiten und Loeschen wird die komplette Liste gespeichert.

Die Eindeutigkeit wird aktuell über `taskdescription` hergestellt. Es gibt keine technische Task-ID.

## Speicherung

Die Persistenz liegt in `TaskStorageService`.

Der konfigurierte Speicherpfad lautet:

```text
data/tasks.json
```

Dieser Pfad ist relativ zum Arbeitsverzeichnis des Backend-Prozesses. Bei normalem Start mit:

```sh
cd backend
mvn spring-boot:run
```

wird deshalb diese Datei verwendet:

```text
backend/data/tasks.json
```

Wenn das Backend aus einem anderen Arbeitsverzeichnis gestartet wird, kann entsprechend eine andere `data/tasks.json` verwendet werden. Im Repository existiert auch `data/tasks.json` im Projektroot; diese Datei gehoert zu Starts aus dem Root-Verzeichnis und ist nicht dieselbe Datei wie `backend/data/tasks.json`.

Die Speicherung ist dateibasiert, nicht datenbankbasiert. Der MySQL-Connector ist zwar in `pom.xml` eingetragen, aber es gibt aktuell keine aktive Datasource-Konfiguration, keine Entity, kein Repository und keine JDBC-/JPA-Logik.

Konsequenzen:

- Tasks bleiben nach einem normalen Backend-Neustart erhalten, solange dieselbe JSON-Datei verwendet wird.
- Die komplette Liste wird bei jeder Änderung neu geschrieben.
- Parallele Schreibzugriffe mehrerer Backend-Instanzen sind nicht abgesichert.
- Die Speicherung ist fuer ein Schul-/Demo-Projekt geeignet, aber keine robuste Produktionspersistenz.

## Frontend

### Start

```sh
cd frontend
npm install
npm run dev
```

Vite startet normalerweise unter:

```text
http://localhost:5173
```

### Verhalten

Die Hauptkomponente liegt in `frontend/src/App.jsx`.

Das Frontend:

- lädt Tasks beim Mounten mit `GET http://localhost:8080/`
- erstellt Tasks mit `POST http://localhost:8080/tasks`
- löscht Tasks mit `POST http://localhost:8080/delete`

Der Update-Endpoint existiert im Backend, wird im aktuellen Frontend aber noch nicht verwendet.

Nach Erstellen oder Loeschen setzt das Frontend `window.location.href = "/"`. Dadurch wird die Seite neu geladen und die Liste erneut vom Backend abgefragt.

Wenn das Backend nicht läuft, schlagen die Fetch-Requests fehl. Das Frontend loggt den Fehler in der Browser-Konsole, zeigt aber aktuell keine eigene Fehlermeldung in der UI an.

## Mehrere Frontends

Mehrere Browserfenster greifen auf dieselbe Backend-Liste und dieselbe JSON-Datei zu, solange sie dasselbe Backend verwenden.

Wichtig:

- Ein Browserfenster aktualisiert sich nicht automatisch, wenn ein anderes Fenster einen Task erstellt oder löscht.
- Es gibt kein Polling, keine WebSockets und keine automatische Synchronisierung.
- Nach einem Reload lädt das Fenster den aktuellen Stand erneut vom Backend.

## Mehrere Backends

Wenn mehrere Backend-Instanzen parallel laufen, hat jede Instanz ihre eigene Task-Liste im Speicher.

Wenn mehrere Instanzen dieselbe `data/tasks.json` verwenden, koennen Schreibkonflikte entstehen, weil die Datei ohne Locking komplett neu geschrieben wird. Wenn jede Instanz in einem anderen Arbeitsverzeichnis läuft, verwendet sie vermutlich eine eigene JSON-Datei.

Für Load Balancing oder produktive Nutzung waere eine echte Datenbank mit sauberer Transaktionslogik nötig.

## Tests

Backend-Tests liegen unter:

```text
backend/src/test/java/com/example/demo/
```

Ausführen:

```sh
cd backend
mvn test
```

Frontend-Tests liegen unter:

```text
frontend/test/
```

Ausführen:

```sh
cd frontend
npm test
```

## Builds

Frontend-Build:

```sh
cd frontend
npm run build
```

Ergebnis:

```text
frontend/dist/
```

Backend-Build:

```sh
cd backend
mvn -B clean package
```

Ergebnis:

```text
backend/target/*.jar
```

### Warum eine JAR und keine WAR?

Das Backend erzeugt aktuell eine ausführbare Spring-Boot-JAR.

Der Grund ist die Maven-Konfiguration in `backend/pom.xml`: Es gibt dort kein explizites Packaging wie:

```xml
<packaging>war</packaging>
```

Ohne explizites Packaging verwendet Maven standardmaessig `jar`. Das passt zur aktuellen Architektur, weil die Anwendung als eigenstaendige Spring-Boot-App mit eingebettetem Tomcat laeuft. Die Startklasse `DemoApplication` enthaelt eine normale `main`-Methode:

```java
SpringApplication.run(DemoApplication.class, args);
```

Eine WAR-Datei waere dann sinnvoll, wenn das Backend nicht selbststaendig laufen soll, sondern in einen externen Servlet-Container wie Tomcat auf einer VM deployed wird. Dafuer muesste das Projekt gezielt auf WAR-Packaging umgestellt werden, typischerweise mit `<packaging>war</packaging>` und einer passenden Servlet-Initialisierung.

## GitHub Actions

Der Workflow liegt in:

```text
.github/workflows/pr-build.yml
```

Aktueller Stand:

- Ausführung bei Push auf `main`
- manueller Start via `workflow_dispatch`
- `ubuntu-24.04` als Runner
- Frontend-Job mit Node.js 22
- Backend-Job mit Java 21
- offizielle Actions mit Node-24-Runtime-Majors:
  - `actions/checkout@v6`
  - `actions/setup-node@v6`
  - `actions/setup-java@v5`
  - `actions/upload-artifact@v6`

Der Backend-Job verwendet bewusst:

```sh
mvn -B clean package
```

und nicht:

```sh
./mvnw clean package
```

Grund: Der Maven Wrapper ist fuer den CI-Build nicht notwendig und kann beim Download der konfigurierten Maven-Version mit HTTP 403 scheitern. Auf dem GitHub-hosted Runner ist Maven bereits installiert.

Nach erfolgreichen Builds werden diese Artefakte hochgeladen:

```text
frontend-dist
backend-jar
```

Sie sind im jeweiligen GitHub Actions Lauf unten im Bereich `Artifacts` als ZIP-Dateien downloadbar.

## Bekannte Grenzen

- Keine Task-ID; `taskdescription` ist faktisch der eindeutige Schlüssel.
- Keine Bearbeiten-Funktion im Frontend, obwohl das Backend `/update` anbietet.
- Keine UI-Fehleranzeige, wenn das Backend nicht erreichbar ist.
- Keine automatische Aktualisierung zwischen mehreren offenen Frontends.
- Dateibasierte Speicherung ohne Locking.
- MySQL-Abhängigkeit ist vorhanden, aber nicht aktiv genutzt.

## Sinnvolle nächste Schritte

1. Task-ID einfuehren, damit gleiche Beschreibungen moeglich werden.
2. Bearbeiten-Funktion im Frontend an den `/update`-Endpoint anschliessen.
3. Fehlerzustände im Frontend sichtbar anzeigen.
4. Speicherpfad konfigurierbar machen, zum Beispiel per Spring Property.
5. MySQL-Abhängigkeit entfernen oder echte Datenbankpersistenz mit Spring Data JPA implementieren.
