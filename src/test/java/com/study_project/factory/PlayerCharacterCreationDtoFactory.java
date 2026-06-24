package com.study_project.factory;

import com.study_project.controller.dto.PlayerCharacterCreationDto;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class PlayerCharacterCreationDtoFactory {

	public static PlayerCharacterCreationDtoFactory.Builder aPlayerCharacterCreationDto() {
		return new PlayerCharacterCreationDtoFactory.Builder();
	}

	public static class Builder {
		private String name = "johnny";
		private Short strength = 10;
		private Short dexterity = 10;
		private Short intelligence = 10;
		private Short constitution = 10;
		private Short willpower = 10;
		private Short perception = 10;
		private Short luck = 10;

		public PlayerCharacterCreationDtoFactory.Builder withName(String name) {
			this.name = name;
			return this;
		}

		public PlayerCharacterCreationDtoFactory.Builder withStrength(Short strength) {
			this.strength = strength;
			return this;
		}

		public PlayerCharacterCreationDtoFactory.Builder withDexterity(Short dexterity) {
			this.dexterity = dexterity;
			return this;
		}

		public PlayerCharacterCreationDtoFactory.Builder withIntelligence(Short intelligence) {
			this.intelligence = intelligence;
			return this;
		}

		public PlayerCharacterCreationDtoFactory.Builder withConstitution(Short constitution) {
			this.constitution = constitution;
			return this;
		}

		public PlayerCharacterCreationDtoFactory.Builder withWillpower(Short willpower) {
			this.willpower = willpower;
			return this;
		}

		public PlayerCharacterCreationDtoFactory.Builder withPerception(Short perception) {
			this.perception = perception;
			return this;
		}

		public PlayerCharacterCreationDtoFactory.Builder withLuck(Short luck) {
			this.luck = luck;
			return this;
		}

		public PlayerCharacterCreationDto build() {
			PlayerCharacterCreationDto playerCharacterCreationDto = new PlayerCharacterCreationDto();
			playerCharacterCreationDto.setName(name);
			playerCharacterCreationDto.setStrength(strength);
			playerCharacterCreationDto.setDexterity(dexterity);
			playerCharacterCreationDto.setIntelligence(intelligence);
			playerCharacterCreationDto.setConstitution(constitution);
			playerCharacterCreationDto.setWillpower(willpower);
			playerCharacterCreationDto.setPerception(perception);
			playerCharacterCreationDto.setLuck(luck);
			return playerCharacterCreationDto;
		}
	}

}
