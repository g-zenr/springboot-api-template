package com.template.api.feature.user;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Public user profile")
public record UserResponse(
		@Schema(description = "User id") UUID id,
		@Schema(description = "Username") String username,
		@Schema(description = "Roles") List<String> roles,
		@Schema(description = "Whether account is enabled") boolean enabled,
		@Schema(description = "Created timestamp") Instant createdAt
) {

	public static UserResponse from(AppUser user) {
		return new UserResponse(
				user.getId(),
				user.getUsername(),
				user.roleList(),
				user.isEnabled(),
				user.getCreatedAt());
	}
}
