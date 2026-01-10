package com.study_project.validation.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenericErrorException extends Exception {

	private final String message;

}
