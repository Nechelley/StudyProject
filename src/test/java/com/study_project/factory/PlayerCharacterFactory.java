package com.study_project.factory;

import com.study_project.model.PlayerCharacter;
import com.study_project.model.UnitAttributes;
import com.study_project.model.User;
import com.study_project.repository.PlayerCharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;

@Component
public class PlayerCharacterFactory {

	@Autowired
	private PlayerCharacterRepository playerCharacterRepository;

	private static final String NAME = "Jimmy";
	private static final String SECONDARY_NAME = "Zidaka";
	public static final short INITIAL_LEVEL = 10;
	public static final short MAXIMUM_ATTRIBUTE_VALUE = 15;
	public static final short MINIMUM_ATTRIBUTE_VALUE = 10;

	public PlayerCharacter generateWithHighStrength(User user) {
		PlayerCharacter playerCharacter = new PlayerCharacter();
		playerCharacter.setName(NAME);
		playerCharacter.setCreatedAt(LocalDateTime.now().minusDays(1));

		playerCharacter.setUser(user);
		user.getPlayerCharacters().add(playerCharacter);

		UnitAttributes unitAttributes = new UnitAttributes();
		unitAttributes.setLevel(INITIAL_LEVEL);
		unitAttributes.setStrength(MAXIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setDexterity(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setIntelligence(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setConstitution(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setWillpower(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setPerception(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setLuck(MINIMUM_ATTRIBUTE_VALUE);
		playerCharacter.setBaseUnitAttributes(unitAttributes);

		return playerCharacterRepository.save(playerCharacter);
	}

	public PlayerCharacter generateWithHighDexterity(User user) {
		PlayerCharacter playerCharacter = new PlayerCharacter();
		playerCharacter.setName(SECONDARY_NAME);
		playerCharacter.setCreatedAt(LocalDateTime.now().minusDays(1));

		playerCharacter.setUser(user);
		user.getPlayerCharacters().add(playerCharacter);

		UnitAttributes unitAttributes = new UnitAttributes();
		unitAttributes.setLevel(INITIAL_LEVEL);
		unitAttributes.setStrength(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setDexterity(MAXIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setIntelligence(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setConstitution(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setWillpower(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setPerception(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setLuck(MINIMUM_ATTRIBUTE_VALUE);
		playerCharacter.setBaseUnitAttributes(unitAttributes);

		return playerCharacterRepository.save(playerCharacter);
	}

}
