package com.template.api.common.paging;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Minimal body when {@code include=countOnly} — total matching records, no document rows.
 */
@Schema(name = "CountResponse", description = "Count-only result (include=countOnly)")
public record CountResponse(
		@Schema(description = "Total number of matching records", example = "42") long count) {
}
