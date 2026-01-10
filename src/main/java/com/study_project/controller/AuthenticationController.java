package com.study_project.controller;

import com.study_project.service.AuthenticationService;
import com.study_project.validation.exception.UnauthenticatedUserException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study_project.configuration.security.SecurityConfiguration;
import com.study_project.controller.dto.TokenDto;
import com.study_project.controller.dto.UserLoginDto;

@RestController
@RequestMapping("/authentication")
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	public AuthenticationController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping
	public ResponseEntity<TokenDto> authenticate(@RequestBody UserLoginDto userLoginDto) throws UnauthenticatedUserException {
		UsernamePasswordAuthenticationToken authenticationToken = userLoginDto.createAuthenticationToken();
		String token = authenticationService.authenticate(authenticationToken);
		return ResponseEntity.ok(new TokenDto(token, SecurityConfiguration.AUTHENTICATION_TYPE));
	}

}
