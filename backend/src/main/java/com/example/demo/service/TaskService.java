package com.example.demo.service;

import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.TaskUpdateDTO;
import com.example.demo.model.Task;

/**
 * Dieser Service enthaelt die Fachlogik fuer die Todo-Liste.
 * Er entscheidet, wann Tasks hinzugefuegt, bearbeitet, geloescht und gespeichert werden.
 */
@Service
public class TaskService {

	private final TaskStorageService storageService;
	private final List<Task> tasks;

	public TaskService(TaskStorageService storageService) {
		this.storageService = storageService;
		// Beim Start des Backends werden vorhandene gespeicherte Tasks geladen.
		this.tasks = storageService.loadTasks();
	}

	public List<Task> getTasks() {
		System.out.println("API EP '/' returns task-list of size " + tasks.size() + ".");
		if (tasks.size() > 0) {
			int i = 1;
			for (Task task : tasks) {
				System.out.println("-task " + (i++) + ":" + task.getTaskdescription());
			}
		}
		return tasks;
	}

	public void addTask(Task task) {
		for (Task t : tasks) {
			if (t.getTaskdescription().equals(task.getTaskdescription())) {
				System.out.println(">>>task: '" + task.getTaskdescription() + "' already exists!");
				return; // duplicates will be ignored
			}
		}
		System.out.println("...adding task: '" + task.getTaskdescription() + "'");
		tasks.add(task);
		// Nach dem Erstellen wird die aktuelle Task-Liste dauerhaft gespeichert.
		storageService.saveTasks(tasks);
	}

	public void updateTask(TaskUpdateDTO updateRequest) {
		for (Task t : tasks) {
			if (!t.getTaskdescription().equals(updateRequest.getOldTaskdescription())
					&& t.getTaskdescription().equals(updateRequest.getTaskdescription())) {
				System.out.println(">>>task: '" + updateRequest.getTaskdescription() + "' already exists!");
				return;
			}
		}
		for (Task t : tasks) {
			if (t.getTaskdescription().equals(updateRequest.getOldTaskdescription())) {
				System.out.println("...updating task: '" + updateRequest.getOldTaskdescription() + "' to '"
						+ updateRequest.getTaskdescription() + "'");
				t.setTaskdescription(updateRequest.getTaskdescription());
				// Nach dem Bearbeiten wird die aktualisierte Task-Liste dauerhaft gespeichert.
				storageService.saveTasks(tasks);
				return;
			}
		}
		System.out.println(">>>task: '" + updateRequest.getOldTaskdescription() + "' not found!");
	}

	public void deleteTask(Task task) {
		Iterator<Task> it = tasks.iterator();
		while (it.hasNext()) {
			Task t = it.next();
			if (t.getTaskdescription().equals(task.getTaskdescription())) {
				System.out.println("...deleting task: '" + task.getTaskdescription() + "'");
				it.remove();
				// Nach dem Loeschen wird die aktuelle Task-Liste dauerhaft gespeichert.
				storageService.saveTasks(tasks);
				return;
			}
		}
		System.out.println(">>>task: '" + task.getTaskdescription() + "' not found!");
	}

}
