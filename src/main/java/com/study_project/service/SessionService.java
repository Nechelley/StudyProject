package com.study_project.service;

import com.study_project.validation.exception.UnauthenticatedUserException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.study_project.model.User;
import com.study_project.validation.exception.TryingManipulateAnotherUserStuffException;

@Service
public class SessionService {

	public void testIfUserTryingManipulateAnotherUserStuff(User user) throws TryingManipulateAnotherUserStuffException, UnauthenticatedUserException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new UnauthenticatedUserException();
		}

		User loggedUser = (User) authentication.getPrincipal();
		if (loggedUser == null) {
			throw new UnauthenticatedUserException();
		}

		boolean userTryingUpdateAnotherUser = !loggedUser.getId().equals(user.getId());
		if (userTryingUpdateAnotherUser) {
			throw new TryingManipulateAnotherUserStuffException();
		}
	}

	public User getUserFromSession() throws UnauthenticatedUserException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new UnauthenticatedUserException();
		}
		return (User) authentication.getPrincipal();
	}

}
