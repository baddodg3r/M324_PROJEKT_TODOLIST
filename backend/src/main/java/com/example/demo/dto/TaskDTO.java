package com.example.demo.dto;

/**
 * DTO fuer einen Task in der REST-API.
 * Das Frontend verwendet weiterhin das JSON-Feld taskdescription.
 */
public class TaskDTO {

	private String taskdescription;

	public String getTaskdescription() {
		return taskdescription;
	}

	public void setTaskdescription(String taskdescription) {
		this.taskdescription = taskdescription;
	}

}
