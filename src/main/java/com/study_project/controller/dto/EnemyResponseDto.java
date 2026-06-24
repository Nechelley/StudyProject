package com.study_project.controller.dto;

import com.study_project.model.Enemy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@NoArgsConstructor
public class EnemyResponseDto {

	private Long id;
	private String name;
	private short level;
	private short strength;
	private short dexterity;
	private short intelligence;
	private short constitution;
	private short willpower;
	private short perception;
	private short luck;

	public EnemyResponseDto(Enemy enemy) {
		id = enemy.getId();
		name = enemy.getName();
		level = enemy.getBaseUnitAttributes().getLevel();
		strength = enemy.getBaseUnitAttributes().getStrength();
		dexterity = enemy.getBaseUnitAttributes().getDexterity();
		intelligence = enemy.getBaseUnitAttributes().getIntelligence();
		constitution = enemy.getBaseUnitAttributes().getConstitution();
		willpower = enemy.getBaseUnitAttributes().getWillpower();
		perception = enemy.getBaseUnitAttributes().getPerception();
		luck = enemy.getBaseUnitAttributes().getLuck();
	}

	public static Page<EnemyResponseDto> createDtoFromEnemiesList(Page<Enemy> enemies) {
		return enemies.map(EnemyResponseDto::new);
	}

}
