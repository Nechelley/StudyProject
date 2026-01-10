package com.study_project.validation.handle;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.study_project.validation.dto.GenericErrorDto;
import com.study_project.validation.exception.GenericErrorException;

@RestControllerAdvice
public class GenericErrorExceptionHandler {

	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(GenericErrorException.class)
	public GenericErrorDto handle(GenericErrorException exception) {
		return new GenericErrorDto(exception.getMessage());
	}

}
