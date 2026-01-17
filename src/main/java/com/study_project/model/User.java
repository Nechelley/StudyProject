package com.study_project.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.study_project.configuration.security.SecurityPolicy;
import com.study_project.controller.dto.group.OnCreate;
import com.study_project.controller.dto.group.OnPasswordChange;
import com.study_project.controller.dto.group.OnUpdate;
import com.study_project.enums.ProfileEnum;
import jakarta.persistence.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.study_project.model.interfaces.Logable;

@Entity
@Table(name="user")
@Getter
@Setter
@NoArgsConstructor
public class User implements Logable, Serializable, UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotEmpty(groups = {OnCreate.class, OnUpdate.class})
	@Column(nullable = false, length = 100)
	private String name;

	@NotEmpty(groups = {OnCreate.class, OnPasswordChange.class})
	@Column(nullable = false, length = 500)
	private String password;//<TODO> make a better password policy

	@NotEmpty(groups = OnCreate.class)
	@Email
	@Column(unique = true, length = 100)
	private String email;

	private boolean credentialsNonExpired = true;

	@PastOrPresent
	@Column(nullable = false)
	private LocalDateTime passwordChangedAt;

	@PastOrPresent
	@Column(nullable = false)
	private LocalDateTime accountCreatedAt;

	@PositiveOrZero
	private int tokenVersion = 0;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="user_x_profile",
			joinColumns = {@JoinColumn(name="user_id")},
			inverseJoinColumns = {@JoinColumn(name="profile_id")})
	private List<Profile> profiles = new ArrayList<>();

	@OneToMany(mappedBy = "user",
			cascade = CascadeType.ALL,
			orphanRemoval = true,
			fetch = FetchType.EAGER
	)
	private List<Character> characters = new ArrayList<>();

	@Override
	public Long getIdForLog() {
		return id;
	}

	@Override
	@NullMarked
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.profiles;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	@NullMarked
	public String getUsername() {
		return this.email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;//Not used because at this moment don't looked something necessary, maybe in the future
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;//Not used because at this moment don't looked something necessary, maybe in the future
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return true;//Not used because of the Hard Delete, users are deleted not desactived
	}

	public boolean hasAdminProfile() {
		return profiles.stream().anyMatch(auth -> Objects.equals(auth.getAuthority(), ProfileEnum.ADMIN.getName()));
	}

	public void refreshSecurityState() {
		LocalDateTime now = LocalDateTime.now();

		if (passwordChangedAt != null && passwordChangedAt.plusDays(SecurityPolicy.CREDENTIAL_EXPIRATION_DAYS).isBefore(now)) {
			credentialsNonExpired = false;
		}
	}

	public void changePassword(String newPassword) {
		this.password = newPassword;
		this.passwordChangedAt = LocalDateTime.now();
		this.credentialsNonExpired = true;
		incrementTokenVersion();
	}

	private void incrementTokenVersion() {
		this.tokenVersion++;
	}

}
