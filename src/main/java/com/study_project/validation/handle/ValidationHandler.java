package com.study_project.validation.handle;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.study_project.validation.dto.GenericFieldErrorDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestControllerAdvice
public class ValidationHandler {

	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public List<GenericFieldErrorDto> handle(MethodArgumentNotValidException exception) {
		List<GenericFieldErrorDto> formErrors = new ArrayList<>();

		List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
		fieldErrors.forEach(error -> {
			String field = error.getField();
			String message = error.getDefaultMessage();
			formErrors.add(new GenericFieldErrorDto(field, message));
		});

		return formErrors;
	}

	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ConstraintViolationException.class)
	public List<GenericFieldErrorDto> handle(ConstraintViolationException exception) {
		List<GenericFieldErrorDto> formErrors = new ArrayList<>();

		Set<ConstraintViolation<?>> fieldErrors = exception.getConstraintViolations();
		fieldErrors.forEach(error -> {
			String field = error.getPropertyPath().toString();
			String message = error.getMessage();
			formErrors.add(new GenericFieldErrorDto(field.substring(field.lastIndexOf('.') + 1), message));
		});

		return formErrors;
	}

}
