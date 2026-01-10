package com.study_project.validation.handle;

import com.study_project.validation.dto.GenericErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthenticationFailureHandler {

	@ExceptionHandler(AccountExpiredException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public GenericErrorDto handleAccountExpired(AccountExpiredException ex) {
		return new GenericErrorDto("Your account has expired. Please contact support");
	}

	@ExceptionHandler(CredentialsExpiredException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public GenericErrorDto handleCredentialsExpired(CredentialsExpiredException ex) {
		return new GenericErrorDto("Your password has expired. Please change it");
	}

	@ExceptionHandler(LockedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public GenericErrorDto handleLocked(LockedException ex) {
		return new GenericErrorDto("Your account is locked");
	}

	@ExceptionHandler(DisabledException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public GenericErrorDto handleDisabled(DisabledException ex) {
		return new GenericErrorDto("Your account is disabled");
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public GenericErrorDto handleGeneral(AuthenticationException ex) {
		return new GenericErrorDto("Email or password is incorrect");
	}

}