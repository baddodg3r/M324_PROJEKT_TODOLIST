package com.example.demo.dto;

/**
 * DTO fuer das Bearbeiten eines Tasks.
 * oldTaskdescription beschreibt den bisherigen Text, taskdescription den neuen Text.
 */
public class TaskUpdateDTO {

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
