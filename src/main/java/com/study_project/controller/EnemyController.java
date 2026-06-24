package com.study_project.controller;

import com.study_project.controller.dto.EnemyCreationDto;
import com.study_project.controller.dto.EnemyResponseDto;
import com.study_project.controller.dto.EnemyUpdateDto;
import com.study_project.model.Enemy;
import com.study_project.service.EnemyService;
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
@RequestMapping("/enemy")
public class EnemyController {

	private final EnemyService enemyService;

	public EnemyController(EnemyService enemyService) {
		this.enemyService = enemyService;
	}

	@GetMapping
	public ResponseEntity<Page<EnemyResponseDto>> getEnemiesFromUser(@PageableDefault(sort = "name", direction = Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok(EnemyResponseDto.createDtoFromEnemiesList(enemyService.getEnemies(pageable)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<EnemyResponseDto> findOne(@PathVariable Long id) {
		Optional<Enemy> enemy = enemyService.getEnemy(id);

		return enemy.map(c -> ResponseEntity.ok(new EnemyResponseDto(c))).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<EnemyResponseDto> createEnemy(@RequestBody @Valid EnemyCreationDto enemyCreationDto, UriComponentsBuilder uriBuilder) throws GenericErrorException {
		Enemy enemy = enemyCreationDto.createEnemy();

		Enemy createdEnemy;
		try {
			createdEnemy = enemyService.createEnemy(enemy);
		} catch (UnitAttributesNotValidException e) {
			throw new GenericErrorException("Enemy attributes not valid");
		}

		var uri = uriBuilder.path("/enemy/{id}").buildAndExpand(createdEnemy.getId()).toUri();

		return ResponseEntity.created(uri).body(new EnemyResponseDto(createdEnemy));
	}

	@PutMapping("/{id}")
	public ResponseEntity<EnemyResponseDto> update(@PathVariable Long id, @RequestBody @Valid EnemyUpdateDto enemyUpdateDto) throws EntityNonExistentForManipulateException {
		Enemy enemy = enemyUpdateDto.createEnemy();
		enemy.setId(id);
		Enemy updatedEnemy = enemyService.updateEnemy(enemy);

		return ResponseEntity.ok(new EnemyResponseDto(updatedEnemy));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) throws EntityNonExistentForManipulateException {
		enemyService.deleteEnemy(id);
		return ResponseEntity.noContent().build();
	}
}
