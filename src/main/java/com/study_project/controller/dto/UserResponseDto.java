package com.study_project.controller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import com.study_project.model.User;

@Getter
@Setter
@NoArgsConstructor
public class UserResponseDto {

	private Long id;
	private String name;
	private String email;

	public UserResponseDto(User user) {
		id = user.getId();
		name = user.getName();
		email = user.getEmail();
	}

	public static Page<UserResponseDto> createDtoFromUsersList(Page<User> users) {
		return users.map(UserResponseDto::new);
	}

}
