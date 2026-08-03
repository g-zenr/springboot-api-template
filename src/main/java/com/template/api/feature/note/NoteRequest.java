package com.template.api.feature.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Create/update note body")
public record NoteRequest(
		@NotBlank @Size(max = 200)
		@Schema(description = "Note title", example = "Meeting notes", requiredMode = Schema.RequiredMode.REQUIRED)
		String title,
		@Size(max = 4000)
		@Schema(description = "Note body", example = "Discussed Q3 goals")
		String body
) {
}
