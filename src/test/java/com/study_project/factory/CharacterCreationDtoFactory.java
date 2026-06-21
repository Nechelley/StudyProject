package com.study_project.factory;

import com.study_project.controller.dto.CharacterCreationDto;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class CharacterCreationDtoFactory {

	public static CharacterCreationDtoFactory.Builder aCharacterCreationDto() {
		return new CharacterCreationDtoFactory.Builder();
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

		public CharacterCreationDtoFactory.Builder withName(String name) {
			this.name = name;
			return this;
		}

		public CharacterCreationDtoFactory.Builder withStrength(Short strength) {
			this.strength = strength;
			return this;
		}

		public CharacterCreationDtoFactory.Builder withDexterity(Short dexterity) {
			this.dexterity = dexterity;
			return this;
		}

		public CharacterCreationDtoFactory.Builder withIntelligence(Short intelligence) {
			this.intelligence = intelligence;
			return this;
		}

		public CharacterCreationDtoFactory.Builder withConstitution(Short constitution) {
			this.constitution = constitution;
			return this;
		}

		public CharacterCreationDtoFactory.Builder withWillpower(Short willpower) {
			this.willpower = willpower;
			return this;
		}

		public CharacterCreationDtoFactory.Builder withPerception(Short perception) {
			this.perception = perception;
			return this;
		}

		public CharacterCreationDtoFactory.Builder withLuck(Short luck) {
			this.luck = luck;
			return this;
		}

		public CharacterCreationDto build() {
			CharacterCreationDto user = new CharacterCreationDto();
			user.setName(name);
			user.setStrength(strength);
			user.setDexterity(dexterity);
			user.setIntelligence(intelligence);
			user.setConstitution(constitution);
			user.setWillpower(willpower);
			user.setPerception(perception);
			user.setLuck(luck);
			return user;
		}
	}

}
