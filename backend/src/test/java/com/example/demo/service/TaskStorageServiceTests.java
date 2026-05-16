package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Testet die JSON-Datei-Speicherung unabhaengig von der Todo-Fachlogik.
 */
class TaskStorageServiceTests {

	@Test
	void missingStorageFileStartsWithEmptyTaskList(@TempDir Path tempDir) {
		// Beim ersten Start existiert die Datei noch nicht, darum ist die Liste leer.
		TaskStorageService storageService = new TaskStorageService(tempDir.resolve("missing-tasks.json"));

		assertEquals(0, storageService.loadTasks().size());
	}

	@Test
	void damagedStorageFileStartsWithEmptyTaskList(@TempDir Path tempDir) throws IOException {
		// Eine beschaedigte JSON-Datei darf das Backend nicht am Starten hindern.
		Path storageFile = tempDir.resolve("tasks.json");
		Files.writeString(storageFile, "not valid json");
		TaskStorageService storageService = new TaskStorageService(storageFile);

		assertEquals(0, storageService.loadTasks().size());
	}

}
