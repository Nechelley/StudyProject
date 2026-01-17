package com.study_project.service;

import com.study_project.controller.dto.group.OnCreate;
import com.study_project.model.Character;
import com.study_project.model.User;
import com.study_project.repository.CharacterRepository;
import com.study_project.validation.exception.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Validated
public class CharacterService {

	private final SessionService sessionService;
	private final CharacterRepository characterRepository;

	public CharacterService(SessionService sessionService, CharacterRepository characterRepository) {
		this.sessionService = sessionService;
		this.characterRepository = characterRepository;
	}

	public Page<Character> getCharactersFromUser(Pageable pageable) throws UnauthenticatedUserException {
		User loggedUser = sessionService.getUserFromSession();
		return characterRepository.findAllByUserId(pageable, loggedUser.getId());
	}

	public Optional<Character> getCharacter(Long id) throws UnauthenticatedUserException, TryingManipulateAnotherUserStuffException {
		Optional<Character> character = characterRepository.findById(id);
		if (character.isPresent()) {
			sessionService.testIfUserTryingManipulateAnotherUserStuff(character.get().getUser());
		}

		return character;
	}

	@Transactional
	@Validated(OnCreate.class)
	public Character createInitialCharacter(@Valid Character character) throws CharacterAttributesNotValidException, UnauthenticatedUserException {
		character.setCreatedAt(LocalDateTime.now());
		character.getBaseCharacterAttributes().setLevel((short) 1);

		User loggedUser = sessionService.getUserFromSession();
		character.setUser(loggedUser);
		loggedUser.getCharacters().add(character);

		if (!character.getBaseCharacterAttributes().isCharacterAttributesDistributionValid()) {
			throw new CharacterAttributesNotValidException();
		}

		return characterRepository.save(character);
	}

	@Transactional
	public Character updateCharacter(@Valid Character character) throws TryingManipulateAnotherUserStuffException, EntityNonExistentForManipulateException, UnauthenticatedUserException {
		Optional<Character> characterInDatabase = characterRepository.findById(character.getId());
		if (characterInDatabase.isEmpty()) {
			throw new EntityNonExistentForManipulateException();
		}

		sessionService.testIfUserTryingManipulateAnotherUserStuff(characterInDatabase.get().getUser());

		Character characterToUpdate = characterInDatabase.get();

		characterToUpdate.setName(character.getName());

		return characterRepository.save(characterToUpdate);
	}

	public void deleteCharacter(Long id) throws EntityNonExistentForManipulateException, TryingManipulateAnotherUserStuffException, UnauthenticatedUserException {
		Optional<Character> characterInDatabase = characterRepository.findById(id);
		if (characterInDatabase.isEmpty()) {
			throw new EntityNonExistentForManipulateException();
		}

		sessionService.testIfUserTryingManipulateAnotherUserStuff(characterInDatabase.get().getUser());

		characterRepository.deleteById(id);
	}
}
