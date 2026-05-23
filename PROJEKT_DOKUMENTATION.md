# Projekt-Dokumentation: ToDo-Liste

Diese Dokumentation beschreibt den aktuellen Stand der ToDo-Anwendung im Repository. Sie ist gegen den Code in `frontend/`, `backend/` und `.github/workflows/pr-build.yml` abgeglichen.

## Überblick

Die Anwendung besteht aus zwei Teilen:

- `frontend/`: React 19 mit Vite 6
- `backend/`: Spring Boot 3.4.5 mit Java 21

Das Frontend zeigt eine ToDo-Liste an und sendet HTTP-Requests über relative `/api`-Pfade. Lokal leitet Vite diese Requests an das Backend unter `http://localhost:8080` weiter. Im Deployment kann Apache dieselben `/api`-Pfade per Reverse Proxy an das Backend weiterleiten. Das Backend verwaltet Tasks in einer Liste und speichert diese Liste als JSON-Datei.

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

Das alte Verzeichnis `backend/bin/` wurde entfernt. Es enthielt eine veraltete Kopie des Backend-Projekts mit alter Java-17-Konfiguration und gehört nicht zur aktuellen Anwendung.

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
  "taskdescription": "Zu löschender Task"
}
```

Leere Tasktexte werden vom Controller mit `400 Bad Request` abgelehnt.

### Fachlogik

Die Fachlogik liegt in `TaskService`.

Aktuelles Verhalten:

- Beim Start werden gespeicherte Tasks aus der JSON-Datei geladen.
- Neue Tasks werden nur hinzugefügt, wenn noch kein Task mit derselben `taskdescription` existiert.
- Updates werden ignoriert, wenn der neue Text bereits bei einem anderen Task existiert.
- Nach Erstellen, Bearbeiten und Löschen wird die komplette Liste gespeichert.

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

Wenn das Backend aus einem anderen Arbeitsverzeichnis gestartet wird, kann entsprechend eine andere `data/tasks.json` verwendet werden. Im Repository existiert auch `data/tasks.json` im Projektroot; diese Datei gehört zu Starts aus dem Root-Verzeichnis und ist nicht dieselbe Datei wie `backend/data/tasks.json`.

Die Speicherung ist dateibasiert, nicht datenbankbasiert. In `pom.xml` ist keine Datenbank-Dependency eingetragen. Es gibt aktuell keine aktive Datasource-Konfiguration, keine Entity, kein Repository und keine JDBC-/JPA-Logik.

Konsequenzen:

- Tasks bleiben nach einem normalen Backend-Neustart erhalten, solange dieselbe JSON-Datei verwendet wird.
- Die komplette Liste wird bei jeder Änderung neu geschrieben.
- Parallele Schreibzugriffe mehrerer Backend-Instanzen sind nicht abgesichert.
- Die Speicherung ist für ein Schul-/Demo-Projekt geeignet, aber keine robuste Produktionspersistenz.

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

Im lokalen Entwicklungsmodus ist in `frontend/vite.config.js` ein Proxy konfiguriert. Requests an `/api` werden dadurch an `http://localhost:8080` weitergeleitet und das Präfix `/api` wird entfernt.

### Verhalten

Die Hauptkomponente liegt in `frontend/src/App.jsx`.

Das Frontend:

- lädt Tasks beim Mounten mit `GET /api/`
- erstellt Tasks mit `POST /api/tasks`
- löscht Tasks mit `POST /api/delete`

Der Update-Endpoint existiert im Backend, wird im aktuellen Frontend aber noch nicht verwendet.

Nach Erstellen oder Löschen setzt das Frontend `window.location.href = "/"`. Dadurch wird die Seite neu geladen und die Liste erneut vom Backend abgefragt.

Wenn das Backend nicht läuft, schlagen die Fetch-Requests fehl. Das Frontend loggt den Fehler in der Browser-Konsole, zeigt aber aktuell keine eigene Fehlermeldung in der UI an.

## Deployment mit Apache-Reverse-Proxy

Für das Deployment wird das gebaute React/Vite-Frontend statisch über Apache ausgeliefert. Das Backend läuft als Spring-Boot-Anwendung auf Port `8080`.

Das Frontend verwendet bewusst keine fest codierten URLs wie:

```text
http://localhost:8080
```

Der Grund: JavaScript läuft im Browser des Clients. Wenn ein Benutzer die Anwendung zum Beispiel über `http://192.168.1.42` öffnet, würde `http://localhost:8080` auf den Client-Rechner zeigen und nicht auf den Deployment-Server.

Stattdessen verwendet das Frontend relative API-Pfade:

```text
/api/
/api/tasks
/api/delete
```

Apache leitet diese Requests intern an das Backend weiter:

```apache
ProxyPass /api/ http://localhost:8080/
ProxyPassReverse /api/ http://localhost:8080/
```

Damit wird `localhost:8080` serverintern auf dem Deployment-Server ausgewertet. Der Browser spricht nur Apache an.

Die dafür benötigten Apache-Module sind:

```sh
sudo a2enmod proxy
sudo a2enmod proxy_http
```

Nach Änderungen an der Apache-Konfiguration:

```sh
sudo apache2ctl configtest
sudo systemctl reload apache2
```

Ein einfacher Funktionstest auf dem Server:

```sh
curl -i http://localhost/api/
curl -i -X POST http://localhost/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"taskdescription":"Test über Apache API"}'
curl -i http://localhost/api/
```

## Mehrere Frontends

Mehrere Browserfenster greifen auf dieselbe Backend-Liste und dieselbe JSON-Datei zu, solange sie dasselbe Backend verwenden.

Wichtig:

- Ein Browserfenster aktualisiert sich nicht automatisch, wenn ein anderes Fenster einen Task erstellt oder löscht.
- Es gibt kein Polling, keine WebSockets und keine automatische Synchronisierung.
- Nach einem Reload lädt das Fenster den aktuellen Stand erneut vom Backend.

## Mehrere Backends

Wenn mehrere Backend-Instanzen parallel laufen, hat jede Instanz ihre eigene Task-Liste im Speicher.

Wenn mehrere Instanzen dieselbe `data/tasks.json` verwenden, können Schreibkonflikte entstehen, weil die Datei ohne Locking komplett neu geschrieben wird. Wenn jede Instanz in einem anderen Arbeitsverzeichnis läuft, verwendet sie vermutlich eine eigene JSON-Datei.

Für Load Balancing oder produktive Nutzung wäre eine echte Datenbank mit sauberer Transaktionslogik nötig.

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

Ohne explizites Packaging verwendet Maven standardmäßig `jar`. Das passt zur aktuellen Architektur, weil die Anwendung als eigenständige Spring-Boot-App mit eingebettetem Tomcat läuft. Die Startklasse `DemoApplication` enthält eine normale `main`-Methode:

```java
SpringApplication.run(DemoApplication.class, args);
```

Eine WAR-Datei wäre dann sinnvoll, wenn das Backend nicht selbstständig laufen soll, sondern in einen externen Servlet-Container wie Tomcat auf einer VM deployed wird. Dafür müsste das Projekt gezielt auf WAR-Packaging umgestellt werden, typischerweise mit `<packaging>war</packaging>` und einer passenden Servlet-Initialisierung.

## GitHub Actions Pipeline

### Ziel der Pipeline

Für das Projekt wurde eine CI-Pipeline mit GitHub Actions erstellt.

Die Pipeline baut automatisch das React/Vite-Frontend sowie das Spring-Boot-Backend. Dadurch kann geprüft werden, ob die Software weiterhin erfolgreich gebaut werden kann.

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

Die Pipeline besteht aus zwei getrennten Jobs:

| Job | Aufgabe |
| --- | --- |
| `frontend-build` | Baut das React/Vite-Frontend |
| `backend-build` | Baut das Spring-Boot-Backend |

Die Trennung verbessert die Übersichtlichkeit und erleichtert die Fehlersuche.

### Frontend-Build

Für das Frontend wird Node.js 22 verwendet.

Der Workflow führt im Verzeichnis `frontend/` folgende Befehle aus:

```sh
npm ci
npm run build
```

`npm ci` eignet sich besser für CI/CD-Pipelines als `npm install`, weil:

- exakt die Versionen aus `package-lock.json` verwendet werden
- reproduzierbare Builds entstehen
- die Installation schneller und stabiler abläuft

Nach erfolgreichem Build wird der erzeugte `dist`-Ordner als GitHub Actions Artefakt hochgeladen.

### Backend-Build

Das Backend verwendet:

- Java 21
- Maven

Der Workflow führt im Verzeichnis `backend/` folgenden Befehl aus:

```sh
mvn -B clean package
```

Dabei wird eine ausführbare Spring-Boot-JAR-Datei erzeugt.

### Warum wurde Maven direkt verwendet?

Im Projekt ist zwar noch der Maven Wrapper vorhanden, im Workflow wird jedoch das bereits installierte Maven des GitHub-Runners verwendet.

Der Grund dafür ist, dass der Maven Wrapper beim automatischen Download von Maven `3.8.6` mit HTTP-403-Fehlern fehlschlagen kann. Durch die direkte Nutzung von Maven im Runner ist der Build stabiler.

Der Backend-Job verwendet deshalb bewusst:

```sh
mvn -B clean package
```

und nicht:

```sh
./mvnw clean package
```

### Warum wurden feste Versionen verwendet?

Im Workflow wurden feste Versionen definiert:

```yaml
runs-on: ubuntu-24.04
node-version: 22
java-version: 21
```

Dies wurde bewusst gewählt, damit:

- die Build-Umgebung stabil bleibt
- keine unerwarteten Änderungen durch neue `latest`-Versionen entstehen
- reproduzierbare Builds möglich sind

### Warum wurde ubuntu-24.04 statt ubuntu-latest verwendet?

`ubuntu-latest` kann sich mit der Zeit automatisch ändern.

Dadurch könnten Builds plötzlich fehlschlagen, obwohl am Projekt selbst nichts geändert wurde. Mit `ubuntu-24.04` bleibt die Umgebung stabil und nachvollziehbar.

### Caching

Für npm und Maven wurde Caching aktiviert.

Dadurch:

- müssen Abhängigkeiten nicht bei jedem Build neu heruntergeladen werden
- werden spätere Pipeline-Läufe schneller

Im Workflow ist dafür gesetzt:

- Frontend: `cache: npm`
- Backend: `cache: maven`

### Verwendete Actions

Der Workflow verwendet offizielle Actions mit Node-24-Runtime-Majors:

- `actions/checkout@v6`
- `actions/setup-node@v6`
- `actions/setup-java@v5`
- `actions/upload-artifact@v6`

### Artefakte

Nach erfolgreichem Build werden Artefakte hochgeladen.

| Artefakt | Inhalt |
| --- | --- |
| `frontend-dist` | gebautes Frontend aus `frontend/dist` |
| `backend-jar` | ausführbare Spring-Boot-JAR aus `backend/target/*.jar` |

Die Artefakte können direkt im jeweiligen GitHub Actions Lauf im Bereich `Artifacts` als ZIP-Dateien heruntergeladen werden.

## Bekannte Grenzen

- Keine Task-ID; `taskdescription` ist faktisch der eindeutige Schlüssel.
- Keine Bearbeiten-Funktion im Frontend, obwohl das Backend `/update` anbietet.
- Keine UI-Fehleranzeige, wenn das Backend nicht erreichbar ist.
- Keine automatische Aktualisierung zwischen mehreren offenen Frontends.
- Dateibasierte Speicherung ohne Locking.
- Keine Datenbankpersistenz; die Anwendung verwendet aktuell JSON-Datei-Speicherung.

## Sinnvolle nächste Schritte

1. Task-ID einführen, damit gleiche Beschreibungen möglich werden.
2. Bearbeiten-Funktion im Frontend an den `/update`-Endpoint anschließen.
3. Fehlerzustände im Frontend sichtbar anzeigen.
4. Speicherpfad konfigurierbar machen, zum Beispiel per Spring Property.
5. Echte Datenbankpersistenz mit Spring Data JPA implementieren, falls die Anwendung später eine Datenbank verwenden soll.
