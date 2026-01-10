package com.study_project.validation.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenericFieldErrorException extends Exception {

	private final String field;
	private final String error;

}
