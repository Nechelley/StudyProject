package com.study_project.controller.dto;

import com.study_project.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCreationDto {

	@NotBlank
	@Size(min = 5, max = 100)
	private String name;

	@NotBlank
	@Size(min = 10, max = 100)
	@Email
	private String email;

	@NotBlank
	@Size(min = 10, max = 500)
	private String password;

	public User createUser() {
		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPassword(password);
		return user;
	}

}
