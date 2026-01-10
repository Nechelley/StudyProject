package com.study_project.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GenericFieldResponseDto {
	private String field;
	private String message;
}
