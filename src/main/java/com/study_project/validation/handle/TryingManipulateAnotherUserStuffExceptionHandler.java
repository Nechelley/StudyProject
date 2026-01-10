package com.study_project.validation.handle;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.study_project.validation.exception.TryingManipulateAnotherUserStuffException;

@RestControllerAdvice
public class TryingManipulateAnotherUserStuffExceptionHandler {

	@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(TryingManipulateAnotherUserStuffException.class)
	public ResponseEntity<Object> handle() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

}