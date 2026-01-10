package com.study_project.service;

import com.study_project.validation.exception.UnauthenticatedUserException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Service
public class AuthenticationService {
	private final AuthenticationManager authenticationManager;
	private final TokenService tokenService;

	public AuthenticationService(AuthenticationManager authenticationManager, TokenService tokenService) {
		this.authenticationManager = authenticationManager;
		this.tokenService = tokenService;
	}

	public String authenticate(UsernamePasswordAuthenticationToken authenticationToken) throws UnauthenticatedUserException {
		Authentication authentication = authenticationManager.authenticate(authenticationToken);
		return tokenService.createToken(authentication);
	}
}
