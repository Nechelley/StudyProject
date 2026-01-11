package com.study_project.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ChangePasswordDto (
		@NotEmpty String currentPassword,
		@NotEmpty @Size(min = 10, max = 500) String newPassword
){}
