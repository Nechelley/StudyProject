package com.study_project.enums;

import lombok.Getter;

@Getter
public enum AttributeEnum {

	STRENGTH("STRENGTH"),
	DEXTERITY("DEXTERITY"),
	INTELLIGENCE("INTELLIGENCE"),
	CONSTITUTION("CONSTITUTION"),
	WILLPOWER("WILLPOWER"),
	PERCEPTION("PERCEPTION"),
	LUCK("LUCK");

	private final String name;

	AttributeEnum(String name) {
		this.name = name;
	}

}
