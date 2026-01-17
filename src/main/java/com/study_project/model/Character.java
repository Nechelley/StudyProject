package com.study_project.model;

import com.study_project.controller.dto.group.OnCreate;
import com.study_project.model.interfaces.Logable;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

	@NotBlank
	@Column(nullable = false, length = 100)
	private String name;

	@PastOrPresent
	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Valid
	@NotNull(groups = OnCreate.class)
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private CharacterAttributes baseCharacterAttributes;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Override
	public Long getIdForLog() {
		return id;
	}
}
