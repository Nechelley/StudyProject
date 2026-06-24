package com.study_project.repository;

import com.study_project.model.PlayerCharacter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerCharacterRepository extends JpaRepository<PlayerCharacter, Long> {

	Page<PlayerCharacter> findAllByUserId(Pageable pageable, long id);

}
