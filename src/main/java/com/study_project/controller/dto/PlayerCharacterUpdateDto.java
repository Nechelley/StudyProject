package com.study_project.controller.dto;

import com.study_project.model.PlayerCharacter;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class PlayerCharacterUpdateDto {

	@NotBlank
	@Length(min = 5, max = 100)
	private String name;

	public PlayerCharacter createPlayerCharacter() {
		PlayerCharacter playerCharacter = new PlayerCharacter();
		playerCharacter.setName(name);
		return playerCharacter;
	}

}
