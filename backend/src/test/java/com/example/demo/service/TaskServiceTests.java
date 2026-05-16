package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.demo.dto.TaskUpdateDTO;
import com.example.demo.model.Task;

/**
 * Testet die Fachlogik der Todo-Liste inklusive Speichern nach Aenderungen.
 */
class TaskServiceTests {

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

		taskService.updateTask(createUpdateDTO("Alter Text", "Neuer Text"));

		// Nach dem Neustart muss der neue Text aus der Datei geladen werden.
		TaskService restartedTaskService = createTaskService(storageFile);
		assertEquals(1, restartedTaskService.getTasks().size());
		assertEquals("Neuer Text", restartedTaskService.getTasks().get(0).getTaskdescription());
	}

	private TaskService createTaskService(Path storageFile) {
		return new TaskService(new TaskStorageService(storageFile));
	}

	private Task createTask(String taskdescription) {
		Task task = new Task();
		task.setTaskdescription(taskdescription);
		return task;
	}

	private TaskUpdateDTO createUpdateDTO(String oldTaskdescription, String taskdescription) {
		TaskUpdateDTO updateDTO = new TaskUpdateDTO();
		updateDTO.setOldTaskdescription(oldTaskdescription);
		updateDTO.setTaskdescription(taskdescription);
		return updateDTO;
	}

}
