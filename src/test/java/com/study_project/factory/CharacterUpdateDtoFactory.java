package com.study_project.factory;

import com.study_project.controller.dto.CharacterUpdateDto;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class CharacterUpdateDtoFactory {

	public static CharacterUpdateDtoFactory.Builder aCharacterUpdateDto() {
		return new CharacterUpdateDtoFactory.Builder();
	}

	public static class Builder {
		private String name = "johnny";

		public CharacterUpdateDtoFactory.Builder withName(String name) {
			this.name = name;
			return this;
		}

		public CharacterUpdateDto build() {
			CharacterUpdateDto user = new CharacterUpdateDto();
			user.setName(name);
			return user;
		}
	}

}
