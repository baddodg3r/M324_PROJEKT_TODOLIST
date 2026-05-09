# Projekt-Dokumentation: ToDo-Liste

Diese Dokumentation beschreibt, wie Frontend und Backend gestartet werden, wie die Anwendung Daten verarbeitet und warum Tasks verloren gehen, sobald das Backend beendet oder neu gestartet wird.

## Projektstruktur

```text
M324_PROJEKT_TODOLIST/
├── backend/    Spring-Boot-Backend mit REST-API
└── frontend/   React/Vite-Frontend
```

Das Frontend ist eine React-Anwendung. Das Backend ist eine Spring-Boot-Anwendung, die eine einfache REST-API bereitstellt.

## Voraussetzungen

Für das Backend:

- Java 17
- Maven oder der mitgelieferte Maven Wrapper

Für das Frontend:

- Node.js
- npm

## Backend starten

Das Backend wurde erfolgreich mit lokal installiertem Maven gestartet.

Vom Projektroot aus:

```sh
cd backend
mvn spring-boot:run
```

Beim ersten Start lädt Maven benötigte Abhängigkeiten aus Maven Central herunter. Danach startet Spring Boot das Backend auf Port `8080`.

Erfolgreicher Start ist an diesen Logzeilen erkennbar:

```text
Tomcat started on port 8080 (http) with context path '/'
Started DemoApplication
```

Während das Backend läuft, sieht man im Terminal auch die API-Aufrufe des Frontends, zum Beispiel:

```text
API EP '/' returns task-list of size 0.
API EP '/tasks': '{"taskdescription":"TEster"}'
...adding task: 'TEster'
API EP '/' returns task-list of size 1.
-task 1:TEster
API EP '/delete': '{"taskdescription":"TEster"}'
...deleting task: 'TEster'
```

Das Backend wird mit `Ctrl+C` beendet. Wenn Spring Boot sauber herunterfährt, erscheinen Logs wie:

```text
Graceful shutdown complete
BUILD SUCCESS
```

Alternative mit Maven Wrapper:

```sh
cd backend
./mvnw spring-boot:run
```

Falls `./mvnw` nicht ausführbar ist:

```sh
cd backend
sh mvnw spring-boot:run
```

Falls beim Start eine Fehlermeldung zu `.mvn/wrapper/maven-wrapper.properties`, `maven-wrapper.jar` oder `org.apache.maven.wrapper.MavenWrapperMain` erscheint, fehlen die Maven-Wrapper-Dateien im Backend. Der Wrapper erwartet diese Dateien unter:

```text
backend/.mvn/wrapper/
```

In diesem Projekt wurden die fehlenden Dateien aus `backend/bin/.mvn/wrapper/` nach `backend/.mvn/wrapper/` kopiert. Danach funktioniert `./mvnw spring-boot:run` im Ordner `backend`.

Das Backend läuft standardmässig auf:

```text
http://localhost:8080
```

Wichtige Endpoints:

```text
GET  http://localhost:8080/
POST http://localhost:8080/tasks
POST http://localhost:8080/delete
```

## Frontend starten

Vom Projektroot aus:

```sh
cd frontend
npm install
npm run dev
```

Falls `npm` noch nicht installiert ist, meldet openSUSE zum Beispiel:

```text
The program 'npm' can be found in the following package:
  * nodejs-common

Try installing with:
    sudo zypper install nodejs-common
```

In diesem Fall kann Node.js/npm so installiert werden:

```sh
sudo zypper install nodejs-common
```

Bei der Installation wurden in dieser Umgebung die Pakete `nodejs24`, `nodejs-common` und `npm24` installiert.

Danach funktioniert:

```sh
cd frontend
npm install
npm run dev
```

Beim Start zeigt Vite zum Beispiel:

```text
VITE v6.3.5  ready in 400 ms

Local:   http://localhost:5173/
Network: use --host to expose
```

Vite zeigt danach die lokale URL im Terminal an. Üblicherweise ist das:

```text
http://localhost:5173
```

Das Frontend ruft das Backend direkt unter `http://localhost:8080` auf. Das ist in `frontend/src/App.jsx` fest codiert:

```js
fetch("http://localhost:8080/")
fetch("http://localhost:8080/tasks", ...)
fetch("http://localhost:8080/delete", ...)
```

Darum muss das Backend laufen, damit das Frontend Tasks laden, speichern oder löschen kann.

## Wie Daten aktuell gespeichert werden

Die Tasks werden aktuell nicht dauerhaft gespeichert. Es gibt keine Datenbankanbindung und keinen Filewriter.

Im Backend liegt die Task-Liste nur in einer Java-Liste:

```java
private List<Task> tasks = new ArrayList<>();
```

Diese Liste befindet sich im Arbeitsspeicher des laufenden Backend-Prozesses. Das bedeutet:

- Neue Tasks werden nur im RAM gespeichert.
- Es wird nichts in eine Datei geschrieben.
- Es wird nichts in eine Datenbank geschrieben.
- Nach einem Neustart des Backends ist die Liste wieder leer.

Im `pom.xml` ist zwar ein MySQL-Connector eingetragen, aber das reicht nicht für Persistenz. Es fehlt eine aktive Konfiguration wie `spring.datasource...`, ausserdem gibt es keine Entity, kein Repository und keine JDBC-Logik, die Daten in MySQL schreibt.

## Warum Daten verloren gehen, wenn das Backend ausfällt

Die gespeicherten Tasks leben nur solange wie der Backend-Prozess lebt.

Beispielablauf:

1. Backend wird gestartet.
2. Die Variable `tasks` wird als neue leere `ArrayList` erzeugt.
3. User erstellt im Frontend mehrere Tasks.
4. Das Backend fügt diese Tasks in die RAM-Liste ein.
5. Backend wird beendet, crasht oder wird neu gestartet.
6. Der Prozess ist weg, damit ist auch der RAM-Inhalt weg.
7. Backend startet neu.
8. `tasks` wird wieder als neue leere `ArrayList` erzeugt.
9. Das Frontend lädt die Tasks neu und erhält eine leere Liste.

Das ist normales Verhalten bei In-Memory-Speicherung. RAM ist flüchtig und nicht als dauerhafte Datenablage geeignet.

## Was passiert, wenn das Backend nicht läuft

Wenn das Frontend geöffnet ist, aber das Backend nicht erreichbar ist:

- `GET http://localhost:8080/` schlägt fehl.
- Die Task-Liste kann nicht geladen werden.
- Neue Tasks können nicht gespeichert werden.
- Tasks können nicht gelöscht werden.
- Im Browser erscheinen Fehler in der Developer Console, typischerweise `Failed to fetch` oder Verbindungsfehler.

Das Frontend hat aktuell keine eigene Offline-Speicherung. Es kann den Ausfall also nicht abfangen und später synchronisieren.

Wenn das Backend während der Nutzung wegbricht, bleibt die aktuell angezeigte Liste im Browser zunächst sichtbar, weil sie im React-State des geöffneten Frontends liegt. Diese Anzeige ist dann aber nur noch eine alte Kopie. Neue Requests an das Backend schlagen fehl. Nach einem Reload der Seite versucht das Frontend wieder `GET http://localhost:8080/`; wenn das Backend weiterhin down ist, kann keine Liste geladen werden. Wenn das Backend neu gestartet wurde, antwortet es wieder, aber mit einer leeren RAM-Liste.

## Was passiert bei zwei geöffneten Frontends

Wenn zwei Browserfenster oder zwei Benutzer dasselbe Backend verwenden, greifen beide auf dieselbe RAM-Liste im Backend zu.

Beispiel:

1. Frontend A wird geöffnet.
2. Frontend B wird geöffnet.
3. Beide laden die aktuelle Liste vom Backend.
4. Frontend A erstellt einen neuen Task.
5. Der Task wird im Backend in `tasks` gespeichert.
6. Frontend B sieht den neuen Task nicht automatisch sofort, weil es seine Anzeige nur beim Laden der Seite aktualisiert.
7. Wenn Frontend B die Seite neu lädt, wird die Liste erneut vom Backend geladen und der Task erscheint.

Wichtig: Die Daten liegen nicht separat in jedem Frontend. Die zentrale Liste liegt im Backend-RAM.

Auffällig ist deshalb: Zwei Browserfenster aktualisieren sich nicht gegenseitig live. Es gibt im Frontend kein Polling, keine WebSockets und keine automatische Neuabfrage nach Änderungen durch andere Fenster. Jedes Browserfenster hat seinen eigenen React-State und lädt die Daten aktuell nur beim Mounten der Komponente beziehungsweise nach dem Seiten-Reload.

## Was passiert bei zwei geöffneten Backends

Wenn das Backend zweimal gestartet würde, zum Beispiel auf zwei unterschiedlichen Ports oder in zwei Containern, hätte jede Backend-Instanz ihre eigene RAM-Liste.

Beispiel:

```text
Backend 1: eigene ArrayList im RAM
Backend 2: eigene ArrayList im RAM
```

Dann hängt es davon ab, welches Backend das Frontend anspricht:

- Frontend gegen Backend 1 sieht nur Tasks aus Backend 1.
- Frontend gegen Backend 2 sieht nur Tasks aus Backend 2.
- Die Listen synchronisieren sich nicht automatisch.

Das wäre besonders problematisch bei Load Balancing. Wenn Requests abwechselnd auf unterschiedliche Backend-Instanzen verteilt werden, kann ein User scheinbar zufällig verschiedene Task-Listen sehen.

## Was passiert bei zwei gleichen Tasks

Das Backend prüft beim Hinzufügen, ob bereits ein Task mit derselben `taskdescription` existiert:

```java
for (Task t : tasks) {
    if (t.getTaskdescription().equals(task.getTaskdescription())) {
        return "redirect:/";
    }
}
```

Wenn die Beschreibung schon existiert, wird der neue Task ignoriert.

## Aktuelle technische Bewertung

Die Anwendung ist aktuell eine Demo-Anwendung mit In-Memory-State.

Für lokale Tests ist das ausreichend:

- einfache API
- keine Datenbankinstallation nötig
- schneller Start
- leicht verständlicher Code

Für produktive Nutzung ist es nicht ausreichend:

- Daten gehen bei Backend-Neustart verloren.
- Keine dauerhafte Speicherung.
- Keine Synchronisierung zwischen mehreren Backend-Instanzen.
- Keine Benutzertrennung.
- Keine Transaktionssicherheit.
- Keine echte Fehlerbehandlung im Frontend.

## Wie man dauerhafte Speicherung ergänzen könnte

Sinnvolle Optionen:

1. Datei speichern
   - Einfach umzusetzen.
   - Für kleine Demo-Projekte möglich.
   - Nicht ideal für parallele Zugriffe oder produktive Nutzung.

2. H2-Datenbank
   - Gut für Schul-/Demo-Projekte.
   - Kann als Datei gespeichert werden.
   - Einfach mit Spring Data JPA kombinierbar.

3. MySQL oder PostgreSQL
   - Sinnvoll für realistischere Umgebungen.
   - Benötigt DB-Service, Konfiguration, Entity und Repository.
   - Daten bleiben auch nach Backend-Neustart erhalten.

Für dieses Projekt wäre Spring Data JPA mit H2 oder MySQL der naheliegende nächste Schritt.

## Kurzer Testplan

Backend-Datenverlust testen:

1. Backend starten.
2. Frontend starten.
3. Im Frontend einen Task anlegen.
4. Seite neu laden: Task ist noch sichtbar.
5. Backend stoppen.
6. Backend neu starten.
7. Frontend neu laden: Task ist weg.

Zwei Frontends testen:

1. Backend starten.
2. Frontend starten.
3. Frontend in zwei Browserfenstern öffnen.
4. In Fenster A einen Task anlegen.
5. Fenster B beobachten: Der Task erscheint nicht automatisch.
6. Fenster B neu laden: Der Task erscheint.

Backend-Ausfall testen:

1. Frontend offen lassen.
2. Backend stoppen.
3. Im Frontend Task anlegen oder löschen.
4. Browser-Konsole prüfen: Request schlägt fehl.

## Fazit

Die Anwendung speichert Tasks aktuell ausschliesslich im RAM des Backends. Solange das Backend läuft, bleiben die Tasks verfügbar. Sobald das Backend beendet, neu gestartet oder durch einen Crash beendet wird, gehen alle Tasks verloren.

Mehrere Frontends teilen sich dieselbe Backend-Liste, sehen Änderungen aber erst nach erneutem Laden oder nach einer expliziten Aktualisierung. Mehrere Backend-Instanzen würden dagegen getrennte Listen führen, weil jede Instanz ihren eigenen RAM hat.
