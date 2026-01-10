package com.study_project.validation.handle;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.study_project.validation.dto.GenericFieldErrorDto;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ValidationHandler {

	private final MessageSource messageSource;

	public ValidationHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public List<GenericFieldErrorDto> handle(MethodArgumentNotValidException exception) {
		List<GenericFieldErrorDto> formErrors = new ArrayList<>();

		List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
		fieldErrors.forEach(error -> {
			String message = messageSource.getMessage(error, LocaleContextHolder.getLocale());
			formErrors.add(new GenericFieldErrorDto(error.getField(), message));
		});

		return formErrors;
	}

}
