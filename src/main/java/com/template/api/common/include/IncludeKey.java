package com.template.api.common.include;

import java.util.Locale;
import java.util.Optional;

/**
 * Known {@code include} tokens (catalog for clients and docs).
 *
 * <p><b>Usable today:</b> {@link #COUNT}, {@link #COUNT_ONLY} (or omit include for default list).
 * <p><b>Reserved:</b> accepted by the parser, no feature payload until a service implements them.
 * <p>Unknown client tokens are ignored (not errors).
 *
 * <p>Documented in {@code docs/LIST-QUERY.md}. Services opt in with
 * {@link IncludeOptions#has(IncludeKey)} — no controller parsing.
 */
public enum IncludeKey {

	/** Usable — documents + total count ({@code totalElements} / {@code totalPages}). */
	COUNT("count"),

	/** Usable — count only; body {@code { "count": N }}, no document rows. */
	COUNT_ONLY("countOnly"),

	/** Reserved — page navigation extras (future). */
	PAGINATION("pagination"),

	/** Reserved — aggregate summary (future). */
	SUMMARY("summary"),

	/** Reserved — statistics block (future). */
	STATISTICS("statistics"),

	/** Reserved — related entities (future). */
	RELATIONS("relations"),

	/** Reserved — effective permissions (future). */
	PERMISSIONS("permissions"),

	/** Reserved — analytics payloads (future). */
	ANALYTICS("analytics");

	private final String token;

	IncludeKey(String token) {
		this.token = token;
	}

	public String token() {
		return token;
	}

	/**
	 * Resolve a client token to a known key (case-insensitive). Empty if unsupported.
	 */
	public static Optional<IncludeKey> fromToken(String raw) {
		if (raw == null || raw.isBlank()) {
			return Optional.empty();
		}
		String normalized = raw.trim().toLowerCase(Locale.ROOT);
		for (IncludeKey key : values()) {
			if (key.token.toLowerCase(Locale.ROOT).equals(normalized)) {
				return Optional.of(key);
			}
		}
		return Optional.empty();
	}
}
