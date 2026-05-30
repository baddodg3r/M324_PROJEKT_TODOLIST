package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.TaskDTO;
import com.example.demo.dto.TaskUpdateDTO;
import com.example.demo.model.Task;
import com.example.demo.service.TaskService;

/**
 * REST-Controller fuer die Todo-API.
 * Der Controller nimmt HTTP-Requests entgegen und delegiert die Logik an den TaskService.
 * Die Routen sind parallel unter der Legacy-API und unter der versionierten API /api/v1 erreichbar.
 */
@RestController
// "" erhaelt die bisherigen Endpunkte fuer alte Clients, /api/v1 ist die neue versionierte API.
@RequestMapping({ "", "/api/v1" })
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@CrossOrigin
	@GetMapping("/")
	public List<TaskDTO> getTasks() {
		return taskService.getTasks().stream()
				.map(this::toDTO)
				.collect(Collectors.toList());
	}

	@CrossOrigin
	@PostMapping("/tasks")
	public ResponseEntity<String> addTask(@RequestBody TaskDTO taskDTO) {
		if (isBlank(taskDTO.getTaskdescription())) {
			// Ohne Tasktext soll kein Task erstellt und nicht gespeichert werden.
			return ResponseEntity.badRequest().body("taskdescription is required");
		}
		System.out.println("API EP 'tasks': '" + taskDTO.getTaskdescription() + "'");
		taskService.addTask(toTask(taskDTO));
		return ResponseEntity.ok("redirect:/");
	}

	@CrossOrigin
	@PostMapping("/update")
	public ResponseEntity<String> updateTask(@RequestBody TaskUpdateDTO updateRequest) {
		if (isBlank(updateRequest.getOldTaskdescription()) || isBlank(updateRequest.getTaskdescription())) {
			// Fuer ein Update braucht es den alten Tasktext zum Suchen und den neuen Text zum Speichern.
			return ResponseEntity.badRequest().body("oldTaskdescription and taskdescription are required");
		}
		System.out.println("API EP 'update': '" + updateRequest.getOldTaskdescription() + "' -> '"
				+ updateRequest.getTaskdescription() + "'");
		taskService.updateTask(updateRequest);
		return ResponseEntity.ok("redirect:/");
	}

	@CrossOrigin
	@PostMapping("/delete")
	public ResponseEntity<String> deleteTask(@RequestBody TaskDTO taskDTO) {
		if (isBlank(taskDTO.getTaskdescription())) {
			// Ohne Tasktext weiss das Backend nicht, welcher Task geloescht werden soll.
			return ResponseEntity.badRequest().body("taskdescription is required");
		}
		System.out.println("API EP 'delete': '" + taskDTO.getTaskdescription() + "'");
		taskService.deleteTask(toTask(taskDTO));
		return ResponseEntity.ok("redirect:/");
	}

	private TaskDTO toDTO(Task task) {
		TaskDTO taskDTO = new TaskDTO();
		taskDTO.setTaskdescription(task.getTaskdescription());
		return taskDTO;
	}

	private Task toTask(TaskDTO taskDTO) {
		Task task = new Task();
		task.setTaskdescription(taskDTO.getTaskdescription());
		return task;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

}
