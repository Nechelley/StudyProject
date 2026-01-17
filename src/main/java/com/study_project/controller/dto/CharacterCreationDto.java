package com.study_project.controller.dto;

import com.study_project.model.Character;
import com.study_project.model.CharacterAttributes;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class CharacterCreationDto {

	@NotEmpty
	@Length(min = 5, max = 100)
	private String name;
	@Min(10)
	@Max(500)
	private short strength;
	@Min(10)
	@Max(500)
	private short dexterity;
	@Min(10)
	@Max(500)
	private short intelligence;
	@Min(10)
	@Max(500)
	private short constitution;
	@Min(10)
	@Max(500)
	private short willpower;
	@Min(10)
	@Max(500)
	private short perception;
	@Min(10)
	@Max(500)
	private short luck;

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
