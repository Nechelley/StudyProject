package com.study_project.factory;

import com.study_project.controller.dto.EnemyCreationDto;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class EnemyCreationDtoFactory {

	public static EnemyCreationDtoFactory.Builder aEnemyCreationDto() {
		return new EnemyCreationDtoFactory.Builder();
	}

	public static class Builder {
		private String name = "johnny";
		private Short level = 1;
		private Short strength = 10;
		private Short dexterity = 10;
		private Short intelligence = 10;
		private Short constitution = 10;
		private Short willpower = 10;
		private Short perception = 10;
		private Short luck = 10;

		public EnemyCreationDtoFactory.Builder withName(String name) {
			this.name = name;
			return this;
		}

		public EnemyCreationDtoFactory.Builder withLevel(Short level) {
			this.level = level;
			return this;
		}

		public EnemyCreationDtoFactory.Builder withStrength(Short strength) {
			this.strength = strength;
			return this;
		}

		public EnemyCreationDtoFactory.Builder withDexterity(Short dexterity) {
			this.dexterity = dexterity;
			return this;
		}

		public EnemyCreationDtoFactory.Builder withIntelligence(Short intelligence) {
			this.intelligence = intelligence;
			return this;
		}

		public EnemyCreationDtoFactory.Builder withConstitution(Short constitution) {
			this.constitution = constitution;
			return this;
		}

		public EnemyCreationDtoFactory.Builder withWillpower(Short willpower) {
			this.willpower = willpower;
			return this;
		}

		public EnemyCreationDtoFactory.Builder withPerception(Short perception) {
			this.perception = perception;
			return this;
		}

		public EnemyCreationDtoFactory.Builder withLuck(Short luck) {
			this.luck = luck;
			return this;
		}

		public EnemyCreationDto build() {
			EnemyCreationDto enemyCreationDto = new EnemyCreationDto();
			enemyCreationDto.setName(name);
			enemyCreationDto.setLevel(level);
			enemyCreationDto.setStrength(strength);
			enemyCreationDto.setDexterity(dexterity);
			enemyCreationDto.setIntelligence(intelligence);
			enemyCreationDto.setConstitution(constitution);
			enemyCreationDto.setWillpower(willpower);
			enemyCreationDto.setPerception(perception);
			enemyCreationDto.setLuck(luck);
			return enemyCreationDto;
		}
	}

}
