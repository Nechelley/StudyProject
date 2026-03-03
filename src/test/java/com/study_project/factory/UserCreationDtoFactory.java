package com.study_project.factory;

import com.study_project.controller.dto.UserCreationDto;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class UserCreationDtoFactory {

	public static Builder aUserCreationDto() {
		return new Builder();
	}

	public static class Builder {
		private String email = "johnny@example.com";
		private String name = "johnny";
		private String password = "password123";

		public Builder withEmail(String email) {
			this.email = email;
			return this;
		}

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public Builder withPassword(String password) {
			this.password = password;
			return this;
		}

		public UserCreationDto build() {
			UserCreationDto user = new UserCreationDto();
			user.setEmail(email);
			user.setName(name);
			user.setPassword(password);
			return user;
		}
	}
}
