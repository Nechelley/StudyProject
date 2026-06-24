package com.study_project.service;

import com.study_project.controller.dto.group.OnCreate;
import com.study_project.model.Enemy;
import com.study_project.model.PlayerCharacter;
import com.study_project.model.User;
import com.study_project.repository.EnemyRepository;
import com.study_project.repository.PlayerCharacterRepository;
import com.study_project.validation.exception.EntityNonExistentForManipulateException;
import com.study_project.validation.exception.TryingManipulateAnotherUserStuffException;
import com.study_project.validation.exception.UnauthenticatedUserException;
import com.study_project.validation.exception.UnitAttributesNotValidException;
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
public class EnemyService {

	private final EnemyRepository enemyRepository;

	public EnemyService(EnemyRepository enemyRepository) {
		this.enemyRepository = enemyRepository;
	}

	public Page<Enemy> getEnemies(Pageable pageable) {
		return enemyRepository.findAll(pageable);
	}

	public Optional<Enemy> getEnemy(Long id) {
		return enemyRepository.findById(id);
	}

	@Transactional
	@Validated(OnCreate.class)
	public Enemy createEnemy(@Valid Enemy enemy) throws UnitAttributesNotValidException {
		enemy.setCreatedAt(LocalDateTime.now());

		if (!enemy.getBaseUnitAttributes().isCharacterAttributesDistributionValid()) {
			throw new UnitAttributesNotValidException();
		}

		return enemyRepository.save(enemy);
	}

	@Transactional
	public Enemy updateEnemy(@Valid Enemy enemy) throws EntityNonExistentForManipulateException {
		Optional<Enemy> enemyInDatabase = enemyRepository.findById(enemy.getId());
		if (enemyInDatabase.isEmpty()) {
			throw new EntityNonExistentForManipulateException();
		}

		Enemy enemyToUpdate = enemyInDatabase.get();

		enemyToUpdate.setName(enemy.getName());

		return enemyRepository.save(enemyToUpdate);
	}

	public void deleteEnemy(Long id) throws EntityNonExistentForManipulateException {
		Optional<Enemy> enemyInDatabase = enemyRepository.findById(id);
		if (enemyInDatabase.isEmpty()) {
			throw new EntityNonExistentForManipulateException();
		}

		enemyRepository.deleteById(id);
	}

}
