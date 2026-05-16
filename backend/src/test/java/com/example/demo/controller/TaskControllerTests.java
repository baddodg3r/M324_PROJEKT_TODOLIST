package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
	void updateTaskReadsTaskUpdateDTOAndDelegatesToService(@TempDir Path tempDir) throws Exception {
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
	void deleteTaskReadsTaskDTOAndDelegatesToService(@TempDir Path tempDir) throws Exception {
		FakeTaskService taskService = new FakeTaskService(tempDir.resolve("tasks.json"));
		MockMvc mockMvc = createMockMvc(taskService);

		mockMvc.perform(post("/delete")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"taskdescription\":\"Zu loeschen\"}"))
				.andExpect(status().isOk())
				.andExpect(content().string("redirect:/"));

		assertEquals("Zu loeschen", taskService.deletedTask.getTaskdescription());
	}

	private MockMvc createMockMvc(TaskService taskService) {
		return MockMvcBuilders.standaloneSetup(new TaskController(taskService)).build();
	}

	private Task createTask(String taskdescription) {
		Task task = new Task();
		task.setTaskdescription(taskdescription);
		return task;
	}

	private static class FakeTaskService extends TaskService {
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
