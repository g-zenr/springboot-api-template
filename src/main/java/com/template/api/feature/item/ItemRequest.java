package com.template.api.feature.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Create/update item body")
public record ItemRequest(
		@NotBlank @Size(max = 120)
		@Schema(description = "Unique item name", example = "Widget", requiredMode = Schema.RequiredMode.REQUIRED)
		String name,
		@Size(max = 500)
		@Schema(description = "Optional description", example = "Demo item")
		String description
) {
}
