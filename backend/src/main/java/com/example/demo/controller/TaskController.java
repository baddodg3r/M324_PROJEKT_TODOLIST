package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.TaskDTO;
import com.example.demo.dto.TaskUpdateDTO;
import com.example.demo.model.Task;
import com.example.demo.service.TaskService;

/**
 * REST-Controller fuer die Todo-API.
 * Der Controller nimmt HTTP-Requests entgegen und delegiert die Logik an den TaskService.
 */
@RestController
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
	public String addTask(@RequestBody TaskDTO taskDTO) {
		System.out.println("API EP '/tasks': '" + taskDTO.getTaskdescription() + "'");
		taskService.addTask(toTask(taskDTO));
		return "redirect:/";
	}

	@CrossOrigin
	@PostMapping("/update")
	public String updateTask(@RequestBody TaskUpdateDTO updateRequest) {
		System.out.println("API EP '/update': '" + updateRequest.getOldTaskdescription() + "' -> '"
				+ updateRequest.getTaskdescription() + "'");
		taskService.updateTask(updateRequest);
		return "redirect:/";
	}

	@CrossOrigin
	@PostMapping("/delete")
	public String deleteTask(@RequestBody TaskDTO taskDTO) {
		System.out.println("API EP '/delete': '" + taskDTO.getTaskdescription() + "'");
		taskService.deleteTask(toTask(taskDTO));
		return "redirect:/";
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

}
