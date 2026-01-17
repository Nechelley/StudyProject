package com.study_project.controller;

import com.study_project.controller.dto.CharacterCreationDto;
import com.study_project.controller.dto.CharacterUpdateDto;
import com.study_project.controller.dto.CharacterResponseDto;
import com.study_project.model.Character;
import com.study_project.service.CharacterService;
import com.study_project.validation.exception.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@RestController
@RequestMapping("/character")
public class CharacterController {

	private final CharacterService characterService;

	public CharacterController(CharacterService characterService) {
		this.characterService = characterService;
	}

	@GetMapping
	public ResponseEntity<Page<CharacterResponseDto>> getCharactersFromUser(@PageableDefault(sort = "name", direction = Direction.ASC) Pageable pageable) throws UnauthenticatedUserException {
		return ResponseEntity.ok(CharacterResponseDto.createDtoFromCharactersList(characterService.getCharactersFromUser(pageable)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<CharacterResponseDto> findOne(@PathVariable Long id) throws UnauthenticatedUserException, TryingManipulateAnotherUserStuffException {
		Optional<Character> character = characterService.getCharacter(id);

		return character.map(c -> ResponseEntity.ok(new CharacterResponseDto(c))).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<CharacterResponseDto> createInitialCharacter(@RequestBody CharacterCreationDto characterCreationDto, UriComponentsBuilder uriBuilder) throws GenericErrorException, UnauthenticatedUserException {
		Character character = characterCreationDto.createCharacter();

		Character createdCharacter;
		try {
			createdCharacter = characterService.createInitialCharacter(character);
		} catch (CharacterAttributesNotValidException e) {
			throw new GenericErrorException("Character attributes not valid");
		}

		var uri = uriBuilder.path("/character/{id}").buildAndExpand(createdCharacter.getId()).toUri();

		return ResponseEntity.created(uri).body(new CharacterResponseDto(createdCharacter));
	}

	@PutMapping("/{id}")
	public ResponseEntity<CharacterResponseDto> update(@PathVariable Long id, @RequestBody CharacterUpdateDto characterUpdateDto) throws EntityNonExistentForManipulateException, TryingManipulateAnotherUserStuffException, UnauthenticatedUserException {
		Character character = characterUpdateDto.createCharacter();
		character.setId(id);
		Character updatedCharacter = characterService.updateCharacter(character);

		return ResponseEntity.ok(new CharacterResponseDto(updatedCharacter));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) throws TryingManipulateAnotherUserStuffException, EntityNonExistentForManipulateException, UnauthenticatedUserException {
		characterService.deleteCharacter(id);
		return ResponseEntity.noContent().build();
	}
}
