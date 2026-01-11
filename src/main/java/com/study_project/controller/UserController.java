package com.study_project.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.study_project.controller.dto.*;
import com.study_project.validation.exception.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study_project.enums.ProfileEnum;
import com.study_project.model.Profile;
import com.study_project.model.User;
import com.study_project.service.UserService;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public ResponseEntity<Page<UserResponseDto>> getAll(@PageableDefault(sort = "email", direction = Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok(UserResponseDto.createDtoFromUsersList(userService.getUsers(pageable)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDto> findOne(@PathVariable Long id) throws UnauthenticatedUserException, TryingManipulateAnotherUserStuffException {
		Optional<User> user = userService.getUser(id);

		return user.map(u -> ResponseEntity.ok(new UserResponseDto(u))).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<UserResponseDto> createBasic(@RequestBody @Valid UserCreationDto userCreationDto, UriComponentsBuilder uriBuilder) throws EmailAlreadyRegisteredException {
		return createUser(userCreationDto, ProfileEnum.BASIC, uriBuilder);
	}

	@PostMapping("/admin")
	public ResponseEntity<UserResponseDto> createAdmin(@RequestBody @Valid UserCreationDto userCreationDto, UriComponentsBuilder uriBuilder) throws EmailAlreadyRegisteredException {
		return createUser(userCreationDto, ProfileEnum.ADMIN, uriBuilder);
	}

	private ResponseEntity<UserResponseDto> createUser(UserCreationDto userCreationDto, ProfileEnum profileEnum, UriComponentsBuilder uriBuilder) throws EmailAlreadyRegisteredException {
		User user = userCreationDto.createUser();

		List<Profile> profiles = new ArrayList<>();
		profiles.add(new Profile(profileEnum.getId(), profileEnum.getName()));
		user.setProfiles(profiles);

		User createdUser = userService.createUser(user);

		var uri = uriBuilder.path("/user/{id}").buildAndExpand(createdUser.getId()).toUri();

		return ResponseEntity.created(uri).body(new UserResponseDto(createdUser));
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserResponseDto> update(@PathVariable Long id, @RequestBody @Valid UserUpdateDto userUpdateDto) throws EntityNonExistentForManipulateException, TryingManipulateAnotherUserStuffException, UnauthenticatedUserException {
		User user = userUpdateDto.createUser();
		user.setId(id);
		User updatedUser = userService.updateUser(user);

		return ResponseEntity.ok(new UserResponseDto(updatedUser));
	}

	@PutMapping("/{id}/change-password")
	public ResponseEntity<GenericFieldResponseDto> changePassword(@PathVariable Long id, @RequestBody @Valid ChangePasswordDto changePasswordDto) throws EntityNonExistentForManipulateException, TryingManipulateAnotherUserStuffException, UnauthenticatedUserException {
		User user = new User();
		user.setId(id);
		user.setPassword(changePasswordDto.newPassword());

		userService.changePassword(user, changePasswordDto.currentPassword());
		return ResponseEntity.ok(new GenericFieldResponseDto("message", "Password changed successfully"));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) throws TryingManipulateAnotherUserStuffException, EntityNonExistentForManipulateException, UnauthenticatedUserException {
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

}
