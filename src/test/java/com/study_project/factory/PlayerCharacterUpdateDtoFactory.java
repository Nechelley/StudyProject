package com.study_project.factory;

import com.study_project.controller.dto.PlayerCharacterUpdateDto;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class PlayerCharacterUpdateDtoFactory {

	public static PlayerCharacterUpdateDtoFactory.Builder aPlayerCharacterUpdateDto() {
		return new PlayerCharacterUpdateDtoFactory.Builder();
	}

	public static class Builder {
		private String name = "johnny";

		public PlayerCharacterUpdateDtoFactory.Builder withName(String name) {
			this.name = name;
			return this;
		}

		public PlayerCharacterUpdateDto build() {
			PlayerCharacterUpdateDto playerCharacterUpdateDto = new PlayerCharacterUpdateDto();
			playerCharacterUpdateDto.setName(name);
			return playerCharacterUpdateDto;
		}
	}

}
