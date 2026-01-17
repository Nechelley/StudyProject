package com.study_project.model;

import com.study_project.model.interfaces.Logable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name="character_attributes")
@Getter
@Setter
@NoArgsConstructor
public class CharacterAttributes implements Logable, Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Min(1)
	@Max(99)
	private short level;
	@NotNull
	@Min(10)
	@Max(500)
	private short strength;
	@NotNull
	@Min(10)
	@Max(500)
	private short dexterity;
	@NotNull
	@Min(10)
	@Max(500)
	private short intelligence;
	@NotNull
	@Min(10)
	@Max(500)
	private short constitution;
	@NotNull
	@Min(10)
	@Max(500)
	private short willpower;
	@NotNull
	@Min(10)
	@Max(500)
	private short perception;
	@NotNull
	@Min(10)
	@Max(500)
	private short luck;

	private final static short INITIAL_POINTS = 70;
	private final static short POINTS_PER_LEVEL = 5;

	public boolean isCharacterAttributesDistributionValid() {
		return (strength + dexterity + intelligence + constitution + willpower + perception + luck) <= INITIAL_POINTS + (level*POINTS_PER_LEVEL);
	}

	@Override
	public Long getIdForLog() {
		return id;
	}
}
