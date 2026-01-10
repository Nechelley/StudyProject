package com.study_project.enums;

//Equal in database
public enum ProfileEnum {

	ADMIN(1L, "ADMIN"), BASIC(2L, "BASIC");

	private final String name;
	private final Long id;

	ProfileEnum(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Long getId() {
		return id;
	}

}
