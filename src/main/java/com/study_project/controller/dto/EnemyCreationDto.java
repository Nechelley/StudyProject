package com.study_project.controller.dto;

import com.study_project.model.Enemy;
import com.study_project.model.UnitAttributes;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class EnemyCreationDto {

	@NotBlank
	@Length(min = 5, max = 100)
	private String name;
	@NotNull
	@Min(1)
	@Max(99)
	private Short level;
	@NotNull
	@Min(10)
	@Max(500)
	private Short strength;
	@NotNull
	@Min(10)
	@Max(500)
	private Short dexterity;
	@NotNull
	@Min(10)
	@Max(500)
	private Short intelligence;
	@NotNull
	@Min(10)
	@Max(500)
	private Short constitution;
	@NotNull
	@Min(10)
	@Max(500)
	private Short willpower;
	@NotNull
	@Min(10)
	@Max(500)
	private Short perception;
	@NotNull
	@Min(10)
	@Max(500)
	private Short luck;

	public Enemy createEnemy() {
		Enemy enemy = new Enemy();
		enemy.setName(name);

		enemy.setBaseUnitAttributes(new UnitAttributes());
		enemy.getBaseUnitAttributes().setLevel(level);
		enemy.getBaseUnitAttributes().setStrength(strength);
		enemy.getBaseUnitAttributes().setDexterity(dexterity);
		enemy.getBaseUnitAttributes().setIntelligence(intelligence);
		enemy.getBaseUnitAttributes().setConstitution(constitution);
		enemy.getBaseUnitAttributes().setWillpower(willpower);
		enemy.getBaseUnitAttributes().setPerception(perception);
		enemy.getBaseUnitAttributes().setLuck(luck);
		return enemy;
	}

}
