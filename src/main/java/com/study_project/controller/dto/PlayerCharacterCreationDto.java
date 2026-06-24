package com.study_project.controller.dto;

import com.study_project.model.PlayerCharacter;
import com.study_project.model.UnitAttributes;
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
public class PlayerCharacterCreationDto {

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

	public PlayerCharacter createPlayerCharacter() {
		PlayerCharacter playerCharacter = new PlayerCharacter();
		playerCharacter.setName(name);

		playerCharacter.setBaseUnitAttributes(new UnitAttributes());
		playerCharacter.getBaseUnitAttributes().setLevel((short) 1);
		playerCharacter.getBaseUnitAttributes().setStrength(strength);
		playerCharacter.getBaseUnitAttributes().setDexterity(dexterity);
		playerCharacter.getBaseUnitAttributes().setIntelligence(intelligence);
		playerCharacter.getBaseUnitAttributes().setConstitution(constitution);
		playerCharacter.getBaseUnitAttributes().setWillpower(willpower);
		playerCharacter.getBaseUnitAttributes().setPerception(perception);
		playerCharacter.getBaseUnitAttributes().setLuck(luck);
		return playerCharacter;
	}

}
