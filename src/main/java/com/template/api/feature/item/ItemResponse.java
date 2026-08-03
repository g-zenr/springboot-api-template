package com.template.api.feature.item;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Item resource")
public record ItemResponse(
		@Schema(description = "Item id") UUID id,
		@Schema(description = "Unique name") String name,
		@Schema(description = "Description") String description,
		@Schema(description = "Created timestamp") Instant createdAt,
		@Schema(description = "Updated timestamp") Instant updatedAt
) {
}
