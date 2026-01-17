package com.study_project.model;

import com.study_project.controller.dto.group.OnCreate;
import com.study_project.model.interfaces.Logable;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="\"character\"")
@Getter
@Setter
@NoArgsConstructor
public class Character implements Logable, Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotEmpty
	@Length(min = 5, max = 100)
	private String name;
	private LocalDateTime createdAt;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@Valid
	@NotNull(groups = OnCreate.class)
	private CharacterAttributes baseCharacterAttributes;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Override
	public Long getIdForLog() {
		return id;
	}
}
