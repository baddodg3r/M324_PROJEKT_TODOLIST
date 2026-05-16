package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.dto.TaskUpdateDTO;
import com.example.demo.model.Task;
import com.example.demo.service.TaskService;
import com.example.demo.service.TaskStorageService;

@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
		assertTrue(true, "alles gut");
	}

	@Test
	void createdTasksAreLoadedAfterRestart(@TempDir Path tempDir) {
		// Dieser Test simuliert einen Neustart mit zwei TaskService-Instanzen.
		Path storageFile = tempDir.resolve("tasks.json");
		TaskService taskService = createTaskService(storageFile);

		taskService.addTask(createTask("Persistenter Task"));

		// Die zweite Instanz muss den Task aus derselben JSON-Datei wieder laden.
		TaskService restartedTaskService = createTaskService(storageFile);
		assertEquals(1, restartedTaskService.getTasks().size());
		assertEquals("Persistenter Task", restartedTaskService.getTasks().get(0).getTaskdescription());
	}

	@Test
	void deletedTasksStayDeletedAfterRestart(@TempDir Path tempDir) {
		// Erst wird ein Task gespeichert, danach geloescht und wieder gespeichert.
		Path storageFile = tempDir.resolve("tasks.json");
		TaskService taskService = createTaskService(storageFile);
		taskService.addTask(createTask("Zu loeschen"));

		taskService.deleteTask(createTask("Zu loeschen"));

		// Nach dem Neustart darf der geloeschte Task nicht mehr vorhanden sein.
		TaskService restartedTaskService = createTaskService(storageFile);
		assertEquals(0, restartedTaskService.getTasks().size());
	}

	@Test
	void updatedTasksAreLoadedAfterRestart(@TempDir Path tempDir) {
		// Bearbeitete Tasks muessen nach dem Speichern ebenfalls erhalten bleiben.
		Path storageFile = tempDir.resolve("tasks.json");
		TaskService taskService = createTaskService(storageFile);
		taskService.addTask(createTask("Alter Text"));

		taskService.updateTask(createUpdateRequest("Alter Text", "Neuer Text"));

		// Nach dem Neustart muss der neue Text aus der Datei geladen werden.
		TaskService restartedTaskService = createTaskService(storageFile);
		assertEquals(1, restartedTaskService.getTasks().size());
		assertEquals("Neuer Text", restartedTaskService.getTasks().get(0).getTaskdescription());
	}

	@Test
	void missingStorageFileStartsWithEmptyTaskList(@TempDir Path tempDir) {
		// Beim ersten Start existiert die Datei noch nicht, darum ist die Liste leer.
		TaskService taskService = createTaskService(tempDir.resolve("missing-tasks.json"));

		assertEquals(0, taskService.getTasks().size());
	}

	@Test
	void damagedStorageFileStartsWithEmptyTaskList(@TempDir Path tempDir) throws IOException {
		// Eine beschaedigte JSON-Datei darf das Backend nicht am Starten hindern.
		Path storageFile = tempDir.resolve("tasks.json");
		Files.writeString(storageFile, "not valid json");

		TaskService taskService = createTaskService(storageFile);

		assertEquals(0, taskService.getTasks().size());
	}

	private TaskService createTaskService(Path storageFile) {
		return new TaskService(new TaskStorageService(storageFile));
	}

	private Task createTask(String taskdescription) {
		Task task = new Task();
		task.setTaskdescription(taskdescription);
		return task;
	}

	private TaskUpdateDTO createUpdateRequest(String oldTaskdescription, String taskdescription) {
		TaskUpdateDTO updateRequest = new TaskUpdateDTO();
		updateRequest.setOldTaskdescription(oldTaskdescription);
		updateRequest.setTaskdescription(taskdescription);
		return updateRequest;
	}

}
