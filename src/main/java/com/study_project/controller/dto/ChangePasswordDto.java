package com.study_project.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordDto (
		@NotBlank String currentPassword,
		@NotBlank String newPassword
){}
