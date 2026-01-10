package com.study_project.validation.handle;

import com.study_project.validation.exception.UnauthenticatedUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UnauthenticatedUserExceptionHandler {

	@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(UnauthenticatedUserException.class)
	public ResponseEntity<Object> handle() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

}