package com.study_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.study_project.model.Profile;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
