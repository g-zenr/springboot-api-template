package com.template.api.feature.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Change password for current user")
public record ChangePasswordRequest(
		@NotBlank
		@Schema(description = "Current password", requiredMode = Schema.RequiredMode.REQUIRED)
		String currentPassword,
		@NotBlank @Size(min = 8, max = 100)
		@Schema(description = "New password (min 8 chars)", requiredMode = Schema.RequiredMode.REQUIRED)
		String newPassword
) {
}
