package com.study_project.factory;

import com.study_project.controller.dto.EnemyUpdateDto;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class EnemyUpdateDtoFactory {

	public static EnemyUpdateDtoFactory.Builder aEnemyUpdateDto() {
		return new EnemyUpdateDtoFactory.Builder();
	}

	public static class Builder {
		private String name = "johnny";

		public EnemyUpdateDtoFactory.Builder withName(String name) {
			this.name = name;
			return this;
		}

		public EnemyUpdateDto build() {
			EnemyUpdateDto enemyUpdateDto = new EnemyUpdateDto();
			enemyUpdateDto.setName(name);
			return enemyUpdateDto;
		}
	}

}
