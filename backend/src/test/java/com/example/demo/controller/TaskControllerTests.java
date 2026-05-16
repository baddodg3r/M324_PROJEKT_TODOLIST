package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.demo.dto.TaskUpdateDTO;
import com.example.demo.model.Task;
import com.example.demo.service.TaskService;
import com.example.demo.service.TaskStorageService;

/**
 * Testet die REST-Endpunkte und die Umwandlung von JSON-DTOs.
 */
class TaskControllerTests {

	@Test
	void getTasksReturnsTaskDTOs(@TempDir Path tempDir) throws Exception {
		// GET / soll die internen Task-Objekte als TaskDTO-JSON an das Frontend zurueckgeben.
		FakeTaskService taskService = new FakeTaskService(tempDir.resolve("tasks.json"));
		MockMvc mockMvc = createMockMvc(taskService);
		Task task = createTask("Test Task");
		taskService.tasks.add(task);

		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().json("[{\"taskdescription\":\"Test Task\"}]"));
	}

	@Test
	void addTaskReadsTaskDTOAndDelegatesToService(@TempDir Path tempDir) throws Exception {
		// Ein gueltiger Create-Request soll aus dem JSON ein TaskDTO lesen und den Service aufrufen.
		FakeTaskService taskService = new FakeTaskService(tempDir.resolve("tasks.json"));
		MockMvc mockMvc = createMockMvc(taskService);

		mockMvc.perform(post("/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"taskdescription\":\"Neuer Task\"}"))
				.andExpect(status().isOk())
				.andExpect(content().string("redirect:/"));

		assertEquals("Neuer Task", taskService.addedTask.getTaskdescription());
	}

	@Test
	void addTaskReturnsBadRequestForMissingTaskdescription(@TempDir Path tempDir) throws Exception {
		// Ohne taskdescription ist der Request fachlich ungueltig und soll 400 zurueckgeben.
		FakeTaskService taskService = new FakeTaskService(tempDir.resolve("tasks.json"));
		MockMvc mockMvc = createMockMvc(taskService);

		mockMvc.perform(post("/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());

		// Bei einem 400er darf kein Task an den Service weitergegeben werden.
		assertNull(taskService.addedTask);
	}

	@Test
	void addTaskReturnsBadRequestForEmptyTaskdescription(@TempDir Path tempDir) throws Exception {
		// Eine leere taskdescription darf nicht gespeichert werden.
		FakeTaskService taskService = new FakeTaskService(tempDir.resolve("tasks.json"));
		MockMvc mockMvc = createMockMvc(taskService);

		mockMvc.perform(post("/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"taskdescription\":\"\"}"))
				.andExpect(status().isBadRequest());

		// Bei einem 400er darf kein Task an den Service weitergegeben werden.
		assertNull(taskService.addedTask);
	}

	@Test
	void addTaskReturnsBadRequestForInvalidJson(@TempDir Path tempDir) throws Exception {
		// Kaputtes JSON kann nicht in ein DTO umgewandelt werden und muss deshalb 400 liefern.
		FakeTaskService taskService = new FakeTaskService(tempDir.resolve("tasks.json"));
		MockMvc mockMvc = createMockMvc(taskService);

		mockMvc.perform(post("/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("not valid json"))
				.andExpect(status().isBadRequest());

		// Bei kaputtem JSON darf der Service nicht aufgerufen werden.
		assertNull(taskService.addedTask);
	}

	@Test
	void updateTaskReadsTaskUpdateDTOAndDelegatesToService(@TempDir Path tempDir) throws Exception {
		// Ein gueltiger Update-Request enthaelt alten und neuen Tasktext.
		FakeTaskService taskService = new FakeTaskService(tempDir.resolve("tasks.json"));
		MockMvc mockMvc = createMockMvc(taskService);

		mockMvc.perform(post("/update")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"oldTaskdescription\":\"Alt\",\"taskdescription\":\"Neu\"}"))
				.andExpect(status().isOk())
				.andExpect(content().string("redirect:/"));

		assertEquals("Alt", taskService.updatedTask.getOldTaskdescription());
		assertEquals("Neu", taskService.updatedTask.getTaskdescription());
	}

	@Test
	void updateTaskReturnsBadRequestForMissingOldTaskdescription(@TempDir Path tempDir) throws Exception {
		// Ohne alten Tasktext weiss das Backend nicht, welcher Task bearbeitet werden soll.
		FakeTaskService taskService = new FakeTaskService(tempDir.resolve("tasks.json"));
		MockMvc mockMvc = createMockMvc(taskService);

		mockMvc.perform(post("/update")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"taskdescription\":\"Neu\"}"))
				.andExpect(status().isBadRequest());

		// Bei einem 400er darf kein Update an den Service weitergegeben werden.
		assertNull(taskService.updatedTask);
	}

	@Test
	void updateTaskReturnsBadRequestForMissingTaskdescription(@TempDir Path tempDir) throws Exception {
		// Ohne neuen Tasktext kann kein sinnvoller Update gespeichert werden.
		FakeTaskService taskService = new FakeTaskService(tempDir.resolve("tasks.json"));
		MockMvc mockMvc = createMockMvc(taskService);

		mockMvc.perform(post("/update")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"oldTaskdescription\":\"Alt\"}"))
				.andExpect(status().isBadRequest());

		// Bei einem 400er darf kein Update an den Service weitergegeben werden.
		assertNull(taskService.updatedTask);
	}

	@Test
	void updateTaskReturnsBadRequestForEmptyTaskdescription(@TempDir Path tempDir) throws Exception {
		// Der neue Tasktext darf nicht leer sein.
		FakeTaskService taskService = new FakeTaskService(tempDir.resolve("tasks.json"));
		MockMvc mockMvc = createMockMvc(taskService);

		mockMvc.perform(post("/update")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"oldTaskdescription\":\"Alt\",\"taskdescription\":\"\"}"))
				.andExpect(status().isBadRequest());

		// Bei einem 400er darf kein Update an den Service weitergegeben werden.
		assertNull(taskService.updatedTask);
	}

	@Test
	void deleteTaskReadsTaskDTOAndDelegatesToService(@TempDir Path tempDir) throws Exception {
		// Ein gueltiger Delete-Request enthaelt die taskdescription des zu loeschenden Tasks.
		FakeTaskService taskService = new FakeTaskService(tempDir.resolve("tasks.json"));
		MockMvc mockMvc = createMockMvc(taskService);

		mockMvc.perform(post("/delete")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"taskdescription\":\"Zu loeschen\"}"))
				.andExpect(status().isOk())
				.andExpect(content().string("redirect:/"));

		assertEquals("Zu loeschen", taskService.deletedTask.getTaskdescription());
	}

	@Test
	void deleteTaskReturnsBadRequestForMissingTaskdescription(@TempDir Path tempDir) throws Exception {
		// Ohne taskdescription weiss das Backend nicht, welcher Task geloescht werden soll.
		FakeTaskService taskService = new FakeTaskService(tempDir.resolve("tasks.json"));
		MockMvc mockMvc = createMockMvc(taskService);

		mockMvc.perform(post("/delete")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());

		// Bei einem 400er darf kein Delete an den Service weitergegeben werden.
		assertNull(taskService.deletedTask);
	}

	@Test
	void deleteTaskReturnsBadRequestForEmptyTaskdescription(@TempDir Path tempDir) throws Exception {
		// Ein leerer Tasktext darf nicht als Loeschkriterium verwendet werden.
		FakeTaskService taskService = new FakeTaskService(tempDir.resolve("tasks.json"));
		MockMvc mockMvc = createMockMvc(taskService);

		mockMvc.perform(post("/delete")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"taskdescription\":\"\"}"))
				.andExpect(status().isBadRequest());

		// Bei einem 400er darf kein Delete an den Service weitergegeben werden.
		assertNull(taskService.deletedTask);
	}

	private MockMvc createMockMvc(TaskService taskService) {
		// Standalone MockMvc testet nur den Controller und braucht keinen kompletten Spring-Kontext.
		return MockMvcBuilders.standaloneSetup(new TaskController(taskService)).build();
	}

	private Task createTask(String taskdescription) {
		Task task = new Task();
		task.setTaskdescription(taskdescription);
		return task;
	}

	private static class FakeTaskService extends TaskService {
		// Fake-Service merkt sich nur, welche Daten der Controller weitergibt.
		private final List<Task> tasks = new ArrayList<>();
		private Task addedTask;
		private TaskUpdateDTO updatedTask;
		private Task deletedTask;

		FakeTaskService(Path storageFile) {
			super(new TaskStorageService(storageFile));
		}

		@Override
		public List<Task> getTasks() {
			return tasks;
		}

		@Override
		public void addTask(Task task) {
			this.addedTask = task;
		}

		@Override
		public void updateTask(TaskUpdateDTO updateRequest) {
			this.updatedTask = updateRequest;
		}

		@Override
		public void deleteTask(Task task) {
			this.deletedTask = task;
		}
	}

}
