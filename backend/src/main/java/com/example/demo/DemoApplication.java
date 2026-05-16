package com.example.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is a demo application that provides a RESTful API for a simple ToDo list
 * with JSON file persistence.
 * The endpoint "/" returns a list of tasks.
 * The endpoint "/tasks" adds a new unique task.
 * The endpoint "/update" updates an existing task.
 * The endpoint "/delete" suppresses a task from the list.
 * The task description transferred from the (React) client is provided as a
 * request body in a JSON structure.
 * The data is converted to a task object using Jackson and added to the list of
 * tasks.
 * All endpoints are annotated with @CrossOrigin to enable cross-origin
 * requests.
 *
 * @author luh
 */
@RestController
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	private final ObjectMapper mapper = new ObjectMapper();
	private final Path storageFile;
	private List<Task> tasks;

	public DemoApplication() {
		// Die Tasks werden nachvollziehbar in einer JSON-Datei im Ordner data gespeichert.
		this(Paths.get("data", "tasks.json"));
	}

	DemoApplication(Path storageFile) {
		this.storageFile = storageFile;
		// Beim Start des Backends werden vorhandene gespeicherte Tasks geladen.
		this.tasks = loadTasks();
	}

	@CrossOrigin
	@GetMapping("/")
	public List<Task> getTasks() {

		System.out.println("API EP '/' returns task-list of size " + tasks.size() + ".");
		if (tasks.size() > 0) {
			int i = 1;
			for (Task task : tasks) {
				System.out.println("-task " + (i++) + ":" + task.getTaskdescription());
			}
		}
		return tasks; // actual task list (internally converted to a JSON stream)
	}

	@CrossOrigin
	@PostMapping("/tasks")
	public String addTask(@RequestBody String taskdescription) {
		System.out.println("API EP '/tasks': '" + taskdescription + "'");
		try {
			Task task;
			task = mapper.readValue(taskdescription, Task.class);
			for (Task t : tasks) {
				if (t.getTaskdescription().equals(task.getTaskdescription())) {
					System.out.println(">>>task: '" + task.getTaskdescription() + "' already exists!");
					return "redirect:/"; // duplicates will be ignored
				}
			}
			System.out.println("...adding task: '" + task.getTaskdescription() + "'");
			tasks.add(task);
			// Nach dem Erstellen wird die aktuelle Task-Liste dauerhaft gespeichert.
			saveTasks();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "redirect:/";
	}

	@CrossOrigin
	@PostMapping("/update")
	public String updateTask(@RequestBody String taskUpdateRequest) {
		System.out.println("API EP '/update': '" + taskUpdateRequest + "'");
		try {
			TaskUpdateRequest updateRequest = mapper.readValue(taskUpdateRequest, TaskUpdateRequest.class);
			for (Task t : tasks) {
				if (!t.getTaskdescription().equals(updateRequest.getOldTaskdescription())
						&& t.getTaskdescription().equals(updateRequest.getTaskdescription())) {
					System.out.println(">>>task: '" + updateRequest.getTaskdescription() + "' already exists!");
					return "redirect:/";
				}
			}
			for (Task t : tasks) {
				if (t.getTaskdescription().equals(updateRequest.getOldTaskdescription())) {
					System.out.println("...updating task: '" + updateRequest.getOldTaskdescription() + "' to '"
							+ updateRequest.getTaskdescription() + "'");
					t.setTaskdescription(updateRequest.getTaskdescription());
					// Nach dem Bearbeiten wird die aktualisierte Task-Liste dauerhaft gespeichert.
					saveTasks();
					return "redirect:/";
				}
			}
			System.out.println(">>>task: '" + updateRequest.getOldTaskdescription() + "' not found!");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "redirect:/";
	}

	@CrossOrigin
	@PostMapping("/delete")
	public String delTask(@RequestBody String taskdescription) {
		System.out.println("API EP '/delete': '" + taskdescription + "'");
		try {
			Task task;
			task = mapper.readValue(taskdescription, Task.class);
			Iterator<Task> it = tasks.iterator();
			while (it.hasNext()) {
				Task t = it.next();
				if (t.getTaskdescription().equals(task.getTaskdescription())) {
					System.out.println("...deleting task: '" + task.getTaskdescription() + "'");
					it.remove();
					// Nach dem Loeschen wird die aktuelle Task-Liste dauerhaft gespeichert.
					saveTasks();
					return "redirect:/";
				}
			}
			System.out.println(">>>task: '" + task.getTaskdescription() + "' not found!");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "redirect:/";
	}

	private List<Task> loadTasks() {
		if (!Files.exists(storageFile)) {
			// Wenn die Datei noch nicht existiert, gibt es noch keine gespeicherten Tasks.
			System.out.println("No task storage file found at '" + storageFile + "'. Starting with empty task list.");
			return new ArrayList<>();
		}
		try {
			// Die JSON-Datei wird wieder in eine Java-Liste von Task-Objekten umgewandelt.
			return mapper.readValue(storageFile.toFile(), new TypeReference<List<Task>>() {
			});
		} catch (IOException e) {
			// Bei einer beschaedigten Datei soll das Backend trotzdem starten koennen.
			System.err.println("Could not load task storage file '" + storageFile + "'. Starting with empty task list.");
			System.err.println(e.getMessage());
			return new ArrayList<>();
		}
	}

	private void saveTasks() {
		try {
			Path parent = storageFile.getParent();
			if (parent != null) {
				// Der Ordner data wird automatisch erstellt, falls er noch nicht vorhanden ist.
				Files.createDirectories(parent);
			}
			// Die komplette Task-Liste wird als gut lesbare JSON-Datei gespeichert.
			mapper.writerWithDefaultPrettyPrinter().writeValue(storageFile.toFile(), tasks);
		} catch (IOException e) {
			// Speicherfehler werden geloggt, damit nachvollziehbar ist, warum Daten fehlen koennten.
			System.err.println("Could not save tasks to '" + storageFile + "'.");
			System.err.println(e.getMessage());
		}
	}

	// Hilfsklasse fuer den Update-Endpunkt: alter Text sucht den Task, neuer Text ersetzt ihn.
	private static class TaskUpdateRequest {
		private String oldTaskdescription;
		private String taskdescription;

		public String getOldTaskdescription() {
			return oldTaskdescription;
		}

		public void setOldTaskdescription(String oldTaskdescription) {
			this.oldTaskdescription = oldTaskdescription;
		}

		public String getTaskdescription() {
			return taskdescription;
		}

		public void setTaskdescription(String taskdescription) {
			this.taskdescription = taskdescription;
		}
	}

}
