package com.example.demo.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Testet das TaskDTO fuer die REST-API.
 */
class TaskDTOTests {

	@Test
	void taskdescriptionCanBeSetAndRead() {
		TaskDTO taskDTO = new TaskDTO();

		taskDTO.setTaskdescription("Test Task");

		assertEquals("Test Task", taskDTO.getTaskdescription());
	}

}
