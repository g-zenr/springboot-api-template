package com.template.api.common.include;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parsed, reusable {@code include} query parameter.
 *
 * <p>Rules:
 * <ul>
 *   <li>Comma-separated values</li>
 *   <li>Whitespace ignored</li>
 *   <li>Case-insensitive</li>
 *   <li>Duplicates removed</li>
 *   <li>Unknown tokens ignored (never throws)</li>
 * </ul>
 *
 * <p>Services call {@link #has(IncludeKey)} / {@link #wantsCount()} and only run expensive work
 * when the matching option is present (except default empty → backward-compatible count).
 */
public final class IncludeOptions {

	private static final IncludeOptions EMPTY = new IncludeOptions(EnumSet.noneOf(IncludeKey.class), null);

	private final Set<IncludeKey> keys;
	/** Original non-blank request value (for response echo), or null. */
	private final String raw;

	private IncludeOptions(Set<IncludeKey> keys, String raw) {
		this.keys = Collections.unmodifiableSet(keys);
		this.raw = raw;
	}

	public static IncludeOptions empty() {
		return EMPTY;
	}

	/**
	 * Parse a raw {@code include} query string. Never throws for unknown tokens.
	 */
	public static IncludeOptions parse(String raw) {
		if (raw == null || raw.isBlank()) {
			return EMPTY;
		}
		String trimmedRaw = raw.trim();
		EnumSet<IncludeKey> resolved = EnumSet.noneOf(IncludeKey.class);
		Arrays.stream(trimmedRaw.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(s -> s.toLowerCase(Locale.ROOT))
				.distinct()
				.map(IncludeKey::fromToken)
				.flatMap(Optional::stream)
				.forEach(resolved::add);
		return new IncludeOptions(resolved, trimmedRaw);
	}

	public boolean isEmpty() {
		return keys.isEmpty();
	}

	public boolean has(IncludeKey key) {
		return keys.contains(Objects.requireNonNull(key));
	}

	/**
	 * Whether the expensive total-count query should run for list endpoints.
	 *
	 * <ul>
	 *   <li>{@code include} omitted → {@code true} (unchanged default: full page metadata)</li>
	 *   <li>{@code include} present → {@code true} if {@link IncludeKey#COUNT} or
	 *       {@link IncludeKey#COUNT_ONLY} is listed</li>
	 * </ul>
	 */
	public boolean wantsCount() {
		return isEmpty() || has(IncludeKey.COUNT) || has(IncludeKey.COUNT_ONLY);
	}

	/**
	 * Count-only mode: run {@code COUNT(*)} and return no document rows.
	 */
	public boolean wantsCountOnly() {
		return has(IncludeKey.COUNT_ONLY);
	}

	/**
	 * Whether to load and return document content for list endpoints.
	 * False when {@link IncludeKey#COUNT_ONLY} is present.
	 */
	public boolean wantsContent() {
		return !wantsCountOnly();
	}

	public Set<IncludeKey> keys() {
		return keys;
	}

	/** Raw include string for response echo, or null when omitted. */
	public String raw() {
		return raw;
	}

	/** Canonical joined tokens of recognized keys (stable order of enum), or null if none. */
	public String recognizedCsv() {
		if (keys.isEmpty()) {
			return null;
		}
		return keys.stream().map(IncludeKey::token).collect(Collectors.joining(","));
	}

	@Override
	public String toString() {
		return "IncludeOptions" + keys;
	}
}
