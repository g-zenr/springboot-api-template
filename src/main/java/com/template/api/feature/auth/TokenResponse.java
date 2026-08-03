package com.template.api.feature.auth;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Access + refresh token pair")
public record TokenResponse(
		@Schema(description = "JWT access token") String accessToken,
		@Schema(description = "Opaque refresh token (store securely)") String refreshToken,
		@Schema(description = "Token type", example = "Bearer") String tokenType,
		@Schema(description = "Access token lifetime in milliseconds") long expiresInMs,
		@Schema(description = "Refresh token lifetime in milliseconds") long refreshExpiresInMs,
		@Schema(description = "Authenticated username") String username,
		@Schema(description = "Assigned roles") List<String> roles
) {
}
