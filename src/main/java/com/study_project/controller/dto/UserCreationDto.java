package com.study_project.controller.dto;

import com.study_project.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCreationDto {

	@NotEmpty
	@Size(min = 5, max = 100)
	private String name;
	@NotEmpty
	@Size(min = 10, max = 100)
	@Email
	private String email;
	@NotEmpty
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
