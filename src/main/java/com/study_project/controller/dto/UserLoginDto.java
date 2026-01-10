package com.study_project.controller.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
@Setter
public class UserLoginDto {

	private String email;
	private String password;

	public UsernamePasswordAuthenticationToken createAuthenticationToken() {
		return new UsernamePasswordAuthenticationToken(email, password);
	}

}
