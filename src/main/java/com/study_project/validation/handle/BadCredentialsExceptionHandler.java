package com.study_project.validation.handle;

import com.study_project.validation.dto.GenericFieldErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BadCredentialsExceptionHandler {

	@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(BadCredentialsException.class)
	public GenericFieldErrorDto handle(BadCredentialsException exception) {
		return new GenericFieldErrorDto("message", exception.getMessage());
	}

}