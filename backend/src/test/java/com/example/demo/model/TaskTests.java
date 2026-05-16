package com.example.demo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Testet das Task-Model.
 */
class TaskTests {

	@Test
	void taskdescriptionCanBeSetAndRead() {
		Task task = new Task();

		task.setTaskdescription("Test Task");

		assertEquals("Test Task", task.getTaskdescription());
	}

}
