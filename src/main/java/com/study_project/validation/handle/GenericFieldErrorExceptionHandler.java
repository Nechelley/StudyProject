package com.study_project.validation.handle;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.study_project.validation.dto.GenericFieldErrorDto;
import com.study_project.validation.exception.GenericFieldErrorException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GenericFieldErrorExceptionHandler {

	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(GenericFieldErrorException.class)
	public List<GenericFieldErrorDto> handle(GenericFieldErrorException exception) {
		List<GenericFieldErrorDto> formErrors = new ArrayList<>();

		formErrors.add(new GenericFieldErrorDto(exception.getField(), exception.getError()));

		return formErrors;
	}

}
