package com.study_project.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
@Setter
public class UserLoginDto {

	@NotBlank
	@Size(min = 10, max = 100)
	private String email;

	@NotBlank
	private String password;

	public UsernamePasswordAuthenticationToken createAuthenticationToken() {
		return new UsernamePasswordAuthenticationToken(email, password);
	}

}
