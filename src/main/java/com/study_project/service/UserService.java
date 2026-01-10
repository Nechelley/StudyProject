package com.study_project.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import com.study_project.configuration.security.SecurityConfiguration;
import com.study_project.controller.dto.group.OnPasswordChange;
import com.study_project.controller.dto.group.OnUpdate;
import com.study_project.validation.exception.UnauthenticatedUserException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.study_project.controller.dto.group.OnCreate;
import com.study_project.model.User;
import com.study_project.repository.UserRepository;
import com.study_project.validation.exception.EmailAlreadyRegisteredException;
import com.study_project.validation.exception.EntityNonExistentForManipulateException;
import com.study_project.validation.exception.TryingManipulateAnotherUserStuffException;

@Service
@Validated
public class UserService {

	private final UserRepository userRepository;
	private final SessionService sessionService;

	public UserService(UserRepository userRepository, SessionService sessionService) {
		this.userRepository = userRepository;
		this.sessionService = sessionService;
	}

	public Page<User> getUsers(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	public Optional<User> getUser(Long id) throws UnauthenticatedUserException, TryingManipulateAnotherUserStuffException {
		User loggedUser = sessionService.getUserFromSession();
		boolean userCannotAccess = !Objects.equals(loggedUser.getId(), id) && !loggedUser.hasAdminProfile();
		if (userCannotAccess) {
			throw new TryingManipulateAnotherUserStuffException();
		}

		return userRepository.findById(id);
	}

	@Transactional
	@Validated(OnCreate.class)
	public User createUser(@Valid User user) throws EmailAlreadyRegisteredException {
		user.setPassword(SecurityConfiguration.getEncrypter().encode(user.getPassword()));
		user.setAccountCreatedAt(LocalDateTime.now());
		user.setPasswordChangedAt(LocalDateTime.now());

		testIfUserEmailAlreadyRegistered(user.getEmail());

		return userRepository.save(user);
	}

	@Transactional
	@Validated(OnUpdate.class)
	public User updateUser(@Valid User user) throws TryingManipulateAnotherUserStuffException, EntityNonExistentForManipulateException, UnauthenticatedUserException {
		Optional<User> userInDatabase = userRepository.findById(user.getId());
		if (userInDatabase.isEmpty()) {
			throw new EntityNonExistentForManipulateException();
		}

		sessionService.testIfUserTryingManipulateAnotherUserStuff(user);

		User userToUpdate = userInDatabase.get();

		userToUpdate.setName(user.getName());

		return userRepository.save(userToUpdate);
	}

	@Transactional
	@Validated(OnPasswordChange.class)
	public void changePassword(@Valid User user, String currentPassword) throws TryingManipulateAnotherUserStuffException, EntityNonExistentForManipulateException, UnauthenticatedUserException {
		Optional<User> userInDatabase = userRepository.findById(user.getId());
		if (userInDatabase.isEmpty()) {
			throw new EntityNonExistentForManipulateException();
		}

		sessionService.testIfUserTryingManipulateAnotherUserStuff(user);

		User userToUpdate = userInDatabase.get();
		String newPasswordHash = SecurityConfiguration.getEncrypter().encode(user.getPassword());

		if (!SecurityConfiguration.getEncrypter().matches(currentPassword, newPasswordHash)) {
			throw new BadCredentialsException("Current password is invalid.");
		}

		userToUpdate.changePassword(newPasswordHash);

		userRepository.save(userToUpdate);
	}

	public void deleteUser(Long id) throws EntityNonExistentForManipulateException, TryingManipulateAnotherUserStuffException, UnauthenticatedUserException {
		Optional<User> userInDatabase = userRepository.findById(id);
		if (userInDatabase.isEmpty()) {
			throw new EntityNonExistentForManipulateException();
		}

		sessionService.testIfUserTryingManipulateAnotherUserStuff(userInDatabase.get());

		userRepository.deleteById(id);
	}

	private void testIfUserEmailAlreadyRegistered(String email) throws EmailAlreadyRegisteredException {
		Optional<User> userInDatabase = userRepository.findByEmail(email);

		boolean emailAlreadyRegistered = userInDatabase.isPresent();
		if (emailAlreadyRegistered) {
			throw new EmailAlreadyRegisteredException();
		}
	}

}
