package com.study_project.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import com.study_project.model.User;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {

	private Long id;
	private String name;
	private String email;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String password;

	public UserDto(User user) {
		id = user.getId();
		name = user.getName();
		email = user.getEmail();
	}

	public User createUser() {
		User user = new User();
		user.setId(id);
		user.setName(name);
		user.setEmail(email);
		user.setPassword(password);
		return user;
	}

	public static Page<UserDto> createDtoFromUsersList(Page<User> users) {
		return users.map(UserDto::new);
	}

}
