package com.example.demo.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Testet das TaskUpdateDTO fuer das Bearbeiten eines Tasks.
 */
class TaskUpdateDTOTests {

	@Test
	void oldAndNewTaskdescriptionCanBeSetAndRead() {
		TaskUpdateDTO updateDTO = new TaskUpdateDTO();

		updateDTO.setOldTaskdescription("Alter Text");
		updateDTO.setTaskdescription("Neuer Text");

		assertEquals("Alter Text", updateDTO.getOldTaskdescription());
		assertEquals("Neuer Text", updateDTO.getTaskdescription());
	}

}
