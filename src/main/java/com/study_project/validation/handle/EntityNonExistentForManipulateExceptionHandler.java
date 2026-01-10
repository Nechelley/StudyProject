package com.study_project.validation.handle;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.study_project.validation.exception.EntityNonExistentForManipulateException;

@RestControllerAdvice
public class EntityNonExistentForManipulateExceptionHandler {

	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	@ExceptionHandler(EntityNonExistentForManipulateException.class)
	public ResponseEntity<Object> handle() {
		return ResponseEntity.notFound().build();
	}

}