package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.Task;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Dieser Service ist nur fuer die Datei-Speicherung zustaendig.
 * Die Tasks werden nachvollziehbar als JSON-Datei im Ordner data gespeichert.
 */
@Service
public class TaskStorageService {

	private final ObjectMapper mapper = new ObjectMapper();
	private final Path storageFile;

	public TaskStorageService() {
		this(Paths.get("data", "tasks.json"));
	}

	public TaskStorageService(Path storageFile) {
		this.storageFile = storageFile;
	}

	public List<Task> loadTasks() {
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

	public void saveTasks(List<Task> tasks) {
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

}
