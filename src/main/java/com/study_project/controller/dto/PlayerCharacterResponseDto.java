package com.study_project.controller.dto;

import com.study_project.model.PlayerCharacter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@NoArgsConstructor
public class PlayerCharacterResponseDto {

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

	public PlayerCharacterResponseDto(PlayerCharacter playerCharacter) {
		id = playerCharacter.getId();
		name = playerCharacter.getName();
		level = playerCharacter.getBaseUnitAttributes().getLevel();
		strength = playerCharacter.getBaseUnitAttributes().getStrength();
		dexterity = playerCharacter.getBaseUnitAttributes().getDexterity();
		intelligence = playerCharacter.getBaseUnitAttributes().getIntelligence();
		constitution = playerCharacter.getBaseUnitAttributes().getConstitution();
		willpower = playerCharacter.getBaseUnitAttributes().getWillpower();
		perception = playerCharacter.getBaseUnitAttributes().getPerception();
		luck = playerCharacter.getBaseUnitAttributes().getLuck();
	}

	public static Page<PlayerCharacterResponseDto> createDtoFromPlayerCharactersList(Page<PlayerCharacter> playerCharacters) {
		return playerCharacters.map(PlayerCharacterResponseDto::new);
	}

}
