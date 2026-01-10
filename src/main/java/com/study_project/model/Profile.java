package com.study_project.model;

import java.io.Serializable;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;

import com.study_project.model.interfaces.Logable;

@Entity
@Table(name="profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profile implements GrantedAuthority, Logable, Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotEmpty
	@Length(min = 2, max = 20)
	private String name;

	@Override
	public String getAuthority() {
		return name;
	}

	@Override
	public Long getIdForLog() {
		return id;
	}

}
