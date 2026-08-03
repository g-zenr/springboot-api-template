package com.template.api.feature.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Self-registration request (USER role)")
public record RegisterRequest(
		@NotBlank @Size(min = 3, max = 80)
		@Schema(description = "New username", example = "alice", requiredMode = Schema.RequiredMode.REQUIRED)
		String username,
		@NotBlank @Size(min = 8, max = 100)
		@Schema(description = "Password (min 8 chars)", example = "password1", requiredMode = Schema.RequiredMode.REQUIRED)
		String password
) {
}
