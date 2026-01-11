package com.study_project.controller.dto;

import com.study_project.model.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateDto {

	@NotEmpty
	@Size(min = 5, max = 100)
	private String name;

	public User createUser() {
		User user = new User();
		user.setName(name);
		return user;
	}

}
