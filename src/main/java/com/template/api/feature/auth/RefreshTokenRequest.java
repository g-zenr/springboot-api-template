package com.template.api.feature.auth;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Refresh or logout request body")
public record RefreshTokenRequest(
		@NotBlank @Schema(description = "Opaque refresh token from login", requiredMode = Schema.RequiredMode.REQUIRED)
		String refreshToken
) {
}
