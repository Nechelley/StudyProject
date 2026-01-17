package com.study_project.controller.dto;

import com.study_project.model.Character;
import com.study_project.model.CharacterAttributes;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class CharacterUpdateDto {

	@NotEmpty
	@Length(min = 5, max = 100)
	private String name;

	public Character createCharacter() {
		Character character = new Character();
		character.setName(name);
		return character;
	}

}
