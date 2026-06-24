package com.study_project.factory;

import com.study_project.model.Enemy;
import com.study_project.model.UnitAttributes;
import com.study_project.repository.EnemyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EnemyFactory {

	@Autowired
	private EnemyRepository enemyRepository;

	private static final String NAME = "Jimmy";
	private static final String SECONDARY_NAME = "Zidaka";
	public static final short INITIAL_LEVEL = 10;
	public static final short MAXIMUM_ATTRIBUTE_VALUE = 15;
	public static final short MINIMUM_ATTRIBUTE_VALUE = 10;

	public Enemy generateWithHighStrength() {
		Enemy enemy = new Enemy();
		enemy.setName(NAME);
		enemy.setCreatedAt(LocalDateTime.now().minusDays(1));

		UnitAttributes unitAttributes = new UnitAttributes();
		unitAttributes.setLevel(INITIAL_LEVEL);
		unitAttributes.setStrength(MAXIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setDexterity(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setIntelligence(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setConstitution(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setWillpower(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setPerception(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setLuck(MINIMUM_ATTRIBUTE_VALUE);
		enemy.setBaseUnitAttributes(unitAttributes);

		return enemyRepository.save(enemy);
	}

	public Enemy generateWithHighDexterity() {
		Enemy enemy = new Enemy();
		enemy.setName(SECONDARY_NAME);
		enemy.setCreatedAt(LocalDateTime.now().minusDays(1));

		UnitAttributes unitAttributes = new UnitAttributes();
		unitAttributes.setLevel(INITIAL_LEVEL);
		unitAttributes.setStrength(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setDexterity(MAXIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setIntelligence(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setConstitution(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setWillpower(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setPerception(MINIMUM_ATTRIBUTE_VALUE);
		unitAttributes.setLuck(MINIMUM_ATTRIBUTE_VALUE);
		enemy.setBaseUnitAttributes(unitAttributes);

		return enemyRepository.save(enemy);
	}

}
