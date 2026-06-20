package com.study_project.record;

import com.study_project.model.User;

public record ExecutorUser(
		User user,
		String token
) {
}
