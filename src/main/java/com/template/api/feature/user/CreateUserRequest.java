package com.template.api.feature.user;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Admin create-user request")
public record CreateUserRequest(
		@NotBlank @Size(min = 3, max = 80)
		@Schema(description = "Username", requiredMode = Schema.RequiredMode.REQUIRED)
		String username,
		@NotBlank @Size(min = 8, max = 100)
		@Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED)
		String password,
		@NotEmpty
		@Schema(description = "Roles: ADMIN and/or USER", example = "[\"USER\"]", requiredMode = Schema.RequiredMode.REQUIRED)
		List<@NotBlank String> roles
) {
}
