package com.study_project.controller;

import com.study_project.controller.dto.PlayerCharacterCreationDto;
import com.study_project.controller.dto.PlayerCharacterResponseDto;
import com.study_project.controller.dto.PlayerCharacterUpdateDto;
import com.study_project.model.PlayerCharacter;
import com.study_project.service.PlayerCharacterService;
import com.study_project.validation.exception.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@RestController
@RequestMapping("/player-character")
public class PlayerCharacterController {

	private final PlayerCharacterService playerCharacterService;

	public PlayerCharacterController(PlayerCharacterService playerCharacterService) {
		this.playerCharacterService = playerCharacterService;
	}

	@GetMapping
	public ResponseEntity<Page<PlayerCharacterResponseDto>> getPlayerCharactersFromUser(@PageableDefault(sort = "name", direction = Direction.ASC) Pageable pageable) throws UnauthenticatedUserException {
		return ResponseEntity.ok(PlayerCharacterResponseDto.createDtoFromPlayerCharactersList(playerCharacterService.getPlayerCharactersFromUser(pageable)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<PlayerCharacterResponseDto> findOne(@PathVariable Long id) throws UnauthenticatedUserException, TryingManipulateAnotherUserStuffException {
		Optional<PlayerCharacter> playerCharacter = playerCharacterService.getPlayerCharacter(id);

		return playerCharacter.map(c -> ResponseEntity.ok(new PlayerCharacterResponseDto(c))).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<PlayerCharacterResponseDto> createInitialPlayerCharacter(@RequestBody @Valid PlayerCharacterCreationDto playerCharacterCreationDto, UriComponentsBuilder uriBuilder) throws GenericErrorException, UnauthenticatedUserException {
		PlayerCharacter playerCharacter = playerCharacterCreationDto.createPlayerCharacter();

		PlayerCharacter createdPlayerCharacter;
		try {
			createdPlayerCharacter = playerCharacterService.createInitialPlayerCharacter(playerCharacter);
		} catch (UnitAttributesNotValidException e) {
			throw new GenericErrorException("Player character attributes not valid");
		}

		var uri = uriBuilder.path("/player-character/{id}").buildAndExpand(createdPlayerCharacter.getId()).toUri();

		return ResponseEntity.created(uri).body(new PlayerCharacterResponseDto(createdPlayerCharacter));
	}

	@PutMapping("/{id}")
	public ResponseEntity<PlayerCharacterResponseDto> update(@PathVariable Long id, @RequestBody @Valid PlayerCharacterUpdateDto playerCharacterUpdateDto) throws EntityNonExistentForManipulateException, TryingManipulateAnotherUserStuffException, UnauthenticatedUserException {
		PlayerCharacter playerCharacter = playerCharacterUpdateDto.createPlayerCharacter();
		playerCharacter.setId(id);
		PlayerCharacter updatedPlayerCharacter = playerCharacterService.updatePlayerCharacter(playerCharacter);

		return ResponseEntity.ok(new PlayerCharacterResponseDto(updatedPlayerCharacter));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) throws TryingManipulateAnotherUserStuffException, EntityNonExistentForManipulateException, UnauthenticatedUserException {
		playerCharacterService.deletePlayerCharacter(id);
		return ResponseEntity.noContent().build();
	}
}
