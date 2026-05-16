package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
		assertTrue(true, "alles gut");
	}

	@Test
	void createdTasksAreLoadedAfterRestart(@TempDir Path tempDir) {
		// Dieser Test simuliert einen Neustart mit zwei DemoApplication-Instanzen.
		Path storageFile = tempDir.resolve("tasks.json");
		DemoApplication application = new DemoApplication(storageFile);

		application.addTask("{\"taskdescription\":\"Persistenter Task\"}");

		// Die zweite Instanz muss den Task aus derselben JSON-Datei wieder laden.
		DemoApplication restartedApplication = new DemoApplication(storageFile);
		assertEquals(1, restartedApplication.getTasks().size());
		assertEquals("Persistenter Task", restartedApplication.getTasks().get(0).getTaskdescription());
	}

	@Test
	void deletedTasksStayDeletedAfterRestart(@TempDir Path tempDir) {
		// Erst wird ein Task gespeichert, danach geloescht und wieder gespeichert.
		Path storageFile = tempDir.resolve("tasks.json");
		DemoApplication application = new DemoApplication(storageFile);
		application.addTask("{\"taskdescription\":\"Zu loeschen\"}");

		application.delTask("{\"taskdescription\":\"Zu loeschen\"}");

		// Nach dem Neustart darf der geloeschte Task nicht mehr vorhanden sein.
		DemoApplication restartedApplication = new DemoApplication(storageFile);
		assertEquals(0, restartedApplication.getTasks().size());
	}

	@Test
	void updatedTasksAreLoadedAfterRestart(@TempDir Path tempDir) {
		// Bearbeitete Tasks muessen nach dem Speichern ebenfalls erhalten bleiben.
		Path storageFile = tempDir.resolve("tasks.json");
		DemoApplication application = new DemoApplication(storageFile);
		application.addTask("{\"taskdescription\":\"Alter Text\"}");

		application.updateTask("{\"oldTaskdescription\":\"Alter Text\",\"taskdescription\":\"Neuer Text\"}");

		// Nach dem Neustart muss der neue Text aus der Datei geladen werden.
		DemoApplication restartedApplication = new DemoApplication(storageFile);
		assertEquals(1, restartedApplication.getTasks().size());
		assertEquals("Neuer Text", restartedApplication.getTasks().get(0).getTaskdescription());
	}

	@Test
	void missingStorageFileStartsWithEmptyTaskList(@TempDir Path tempDir) {
		// Beim ersten Start existiert die Datei noch nicht, darum ist die Liste leer.
		DemoApplication application = new DemoApplication(tempDir.resolve("missing-tasks.json"));

		assertEquals(0, application.getTasks().size());
	}

	@Test
	void damagedStorageFileStartsWithEmptyTaskList(@TempDir Path tempDir) throws IOException {
		// Eine beschaedigte JSON-Datei darf das Backend nicht am Starten hindern.
		Path storageFile = tempDir.resolve("tasks.json");
		Files.writeString(storageFile, "not valid json");

		DemoApplication application = new DemoApplication(storageFile);

		assertEquals(0, application.getTasks().size());
	}

}
