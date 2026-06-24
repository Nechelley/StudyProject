package com.study_project.controller.dto;

import com.study_project.model.Enemy;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class EnemyUpdateDto {

	@NotBlank
	@Length(min = 5, max = 100)
	private String name;


	public Enemy createEnemy() {
		Enemy enemy = new Enemy();
		enemy.setName(name);
		return enemy;
	}

}
