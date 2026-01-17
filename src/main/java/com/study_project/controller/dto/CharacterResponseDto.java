package com.study_project.controller.dto;

import com.study_project.model.Character;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@NoArgsConstructor
public class CharacterResponseDto {

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

	public CharacterResponseDto(Character character) {
		id = character.getId();
		name = character.getName();
		level = character.getBaseCharacterAttributes().getLevel();
		strength = character.getBaseCharacterAttributes().getStrength();
		dexterity = character.getBaseCharacterAttributes().getDexterity();
		intelligence = character.getBaseCharacterAttributes().getIntelligence();
		constitution = character.getBaseCharacterAttributes().getConstitution();
		willpower = character.getBaseCharacterAttributes().getWillpower();
		perception = character.getBaseCharacterAttributes().getPerception();
		luck = character.getBaseCharacterAttributes().getLuck();
	}

	public static Page<CharacterResponseDto> createDtoFromCharactersList(Page<Character> characters) {
		return characters.map(CharacterResponseDto::new);
	}

}
