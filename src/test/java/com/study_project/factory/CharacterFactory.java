package com.study_project.factory;

import com.study_project.model.Character;
import com.study_project.model.CharacterAttributes;
import com.study_project.model.User;
import com.study_project.repository.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;

@Component
public class CharacterFactory {

	@Autowired
	private CharacterRepository characterRepository;

	private static final String NAME = "Jimmy";
	private static final String SECONDARY_NAME = "Zidaka";
	public static final short INITIAL_LEVEL = 10;
	public static final short MAXIMUM_ATTRIBUTE_VALUE = 15;
	public static final short MINIMUM_ATTRIBUTE_VALUE = 10;

	public Character generateWithHighStrength(User user) {
		Character character = new Character();
		character.setName(NAME);
		character.setCreatedAt(LocalDateTime.now().minusDays(1));

		character.setUser(user);
		user.getCharacters().add(character);

		CharacterAttributes characterAttributes = new CharacterAttributes();
		characterAttributes.setLevel(INITIAL_LEVEL);
		characterAttributes.setStrength(MAXIMUM_ATTRIBUTE_VALUE);
		characterAttributes.setDexterity(MINIMUM_ATTRIBUTE_VALUE);
		characterAttributes.setIntelligence(MINIMUM_ATTRIBUTE_VALUE);
		characterAttributes.setConstitution(MINIMUM_ATTRIBUTE_VALUE);
		characterAttributes.setWillpower(MINIMUM_ATTRIBUTE_VALUE);
		characterAttributes.setPerception(MINIMUM_ATTRIBUTE_VALUE);
		characterAttributes.setLuck(MINIMUM_ATTRIBUTE_VALUE);
		character.setBaseCharacterAttributes(characterAttributes);

		return characterRepository.save(character);
	}

	public Character generateWithHighDexterity(User user) {
		Character character = new Character();
		character.setName(SECONDARY_NAME);
		character.setCreatedAt(LocalDateTime.now().minusDays(1));

		character.setUser(user);
		user.getCharacters().add(character);

		CharacterAttributes characterAttributes = new CharacterAttributes();
		characterAttributes.setLevel(INITIAL_LEVEL);
		characterAttributes.setStrength(MINIMUM_ATTRIBUTE_VALUE);
		characterAttributes.setDexterity(MAXIMUM_ATTRIBUTE_VALUE);
		characterAttributes.setIntelligence(MINIMUM_ATTRIBUTE_VALUE);
		characterAttributes.setConstitution(MINIMUM_ATTRIBUTE_VALUE);
		characterAttributes.setWillpower(MINIMUM_ATTRIBUTE_VALUE);
		characterAttributes.setPerception(MINIMUM_ATTRIBUTE_VALUE);
		characterAttributes.setLuck(MINIMUM_ATTRIBUTE_VALUE);
		character.setBaseCharacterAttributes(characterAttributes);

		return characterRepository.save(character);
	}

}
