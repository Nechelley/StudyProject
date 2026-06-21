package com.study_project.controller.dto;

import com.study_project.model.Character;
import com.study_project.model.CharacterAttributes;
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
public class CharacterCreationDto {

	@NotBlank
	@Length(min = 5, max = 100)
	private String name;
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

	public Character createCharacter() {
		Character character = new Character();
		character.setName(name);

		character.setBaseCharacterAttributes(new CharacterAttributes());
		character.getBaseCharacterAttributes().setLevel((short) 1);
		character.getBaseCharacterAttributes().setStrength(strength);
		character.getBaseCharacterAttributes().setDexterity(dexterity);
		character.getBaseCharacterAttributes().setIntelligence(intelligence);
		character.getBaseCharacterAttributes().setConstitution(constitution);
		character.getBaseCharacterAttributes().setWillpower(willpower);
		character.getBaseCharacterAttributes().setPerception(perception);
		character.getBaseCharacterAttributes().setLuck(luck);
		return character;
	}

}
