package com.study_project.service;

import org.springframework.stereotype.Service;

import com.study_project.model.Profile;
import com.study_project.repository.ProfileRepository;

import jakarta.transaction.Transactional;

@Service
public class ProfileService {

	private final ProfileRepository profileRepository;

	public ProfileService(ProfileRepository profileRepository) {
		this.profileRepository = profileRepository;
	}

	@Transactional
	public Profile createProfile(Profile profile) {
		return profileRepository.save(profile);
	}

}
