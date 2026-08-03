package com.template.api.feature.note;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Note resource (owner-scoped)")
public record NoteResponse(
		@Schema(description = "Note id") UUID id,
		@Schema(description = "Owner username") String ownerUsername,
		@Schema(description = "Title") String title,
		@Schema(description = "Body") String body,
		@Schema(description = "Created timestamp") Instant createdAt,
		@Schema(description = "Updated timestamp") Instant updatedAt
) {
}
