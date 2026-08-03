package com.template.api.feature.auth;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login credentials")
public record LoginRequest(
		@NotBlank @Schema(description = "Username", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
		String username,
		@NotBlank @Schema(description = "Password", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
		String password
) {
}
