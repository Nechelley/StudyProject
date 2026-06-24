package com.study_project.service;

import com.study_project.controller.dto.group.OnCreate;
import com.study_project.model.PlayerCharacter;
import com.study_project.model.User;
import com.study_project.repository.PlayerCharacterRepository;
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
public class PlayerCharacterService {

	private final SessionService sessionService;
	private final PlayerCharacterRepository playerCharacterRepository;

	public PlayerCharacterService(SessionService sessionService, PlayerCharacterRepository playerCharacterRepository) {
		this.sessionService = sessionService;
		this.playerCharacterRepository = playerCharacterRepository;
	}

	public Page<PlayerCharacter> getPlayerCharactersFromUser(Pageable pageable) throws UnauthenticatedUserException {
		User loggedUser = sessionService.getUserFromSession();
		return playerCharacterRepository.findAllByUserId(pageable, loggedUser.getId());
	}

	public Optional<PlayerCharacter> getPlayerCharacter(Long id) throws UnauthenticatedUserException, TryingManipulateAnotherUserStuffException {
		Optional<PlayerCharacter> playerCharacter = playerCharacterRepository.findById(id);
		if (playerCharacter.isPresent()) {
			sessionService.testIfUserTryingManipulateAnotherUserStuff(playerCharacter.get().getUser());
		}

		return playerCharacter;
	}

	@Transactional
	@Validated(OnCreate.class)
	public PlayerCharacter createInitialPlayerCharacter(@Valid PlayerCharacter playerCharacter) throws UnitAttributesNotValidException, UnauthenticatedUserException {
		playerCharacter.setCreatedAt(LocalDateTime.now());
		playerCharacter.getBaseUnitAttributes().setLevel((short) 1);

		User loggedUser = sessionService.getUserFromSession();
		playerCharacter.setUser(loggedUser);
		loggedUser.getPlayerCharacters().add(playerCharacter);

		if (!playerCharacter.getBaseUnitAttributes().isCharacterAttributesDistributionValid()) {
			throw new UnitAttributesNotValidException();
		}

		return playerCharacterRepository.save(playerCharacter);
	}

	@Transactional
	public PlayerCharacter updatePlayerCharacter(@Valid PlayerCharacter playerCharacter) throws TryingManipulateAnotherUserStuffException, EntityNonExistentForManipulateException, UnauthenticatedUserException {
		Optional<PlayerCharacter> playerCharacterInDatabase = playerCharacterRepository.findById(playerCharacter.getId());
		if (playerCharacterInDatabase.isEmpty()) {
			throw new EntityNonExistentForManipulateException();
		}

		sessionService.testIfUserTryingManipulateAnotherUserStuff(playerCharacterInDatabase.get().getUser());

		PlayerCharacter playerCharacterToUpdate = playerCharacterInDatabase.get();

		playerCharacterToUpdate.setName(playerCharacter.getName());

		return playerCharacterRepository.save(playerCharacterToUpdate);
	}

	public void deletePlayerCharacter(Long id) throws EntityNonExistentForManipulateException, TryingManipulateAnotherUserStuffException, UnauthenticatedUserException {
		Optional<PlayerCharacter> playerCharacterInDatabase = playerCharacterRepository.findById(id);
		if (playerCharacterInDatabase.isEmpty()) {
			throw new EntityNonExistentForManipulateException();
		}

		sessionService.testIfUserTryingManipulateAnotherUserStuff(playerCharacterInDatabase.get().getUser());

		playerCharacterRepository.deleteById(id);
	}
}
