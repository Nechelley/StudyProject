package com.study_project.factory;

import com.study_project.controller.dto.UserUpdateDto;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class UserUpdateDtoFactory {

	public static Builder aUserUpdateDto() {
		return new Builder();
	}

	public static class Builder {
		private String name = "jimmy T";

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public UserUpdateDto build() {
			UserUpdateDto user = new UserUpdateDto();
			user.setName(name);
			return user;
		}
	}
}
