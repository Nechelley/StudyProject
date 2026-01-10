package com.study_project.validation.handle;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.study_project.validation.dto.GenericFieldErrorDto;
import com.study_project.validation.exception.EmailAlreadyRegisteredException;

@RestControllerAdvice
public class EmailAlreadyRegisteredExceptionHandler {

	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	public GenericFieldErrorDto handle(EmailAlreadyRegisteredException exception) {
		return new GenericFieldErrorDto("email", "email already registered");
	}

}
