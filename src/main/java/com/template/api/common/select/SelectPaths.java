package com.template.api.common.select;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parses and validates the reusable {@code select} query parameter.
 *
 * <p>Format: comma-separated paths with optional dots for depth, e.g.
 * {@code id,name,owner.username,owner.roles}.
 */
public final class SelectPaths {

	public static final int MAX_PATHS = 50;
	public static final int MAX_PATH_LENGTH = 120;
	public static final int MAX_DEPTH = 8;

	private SelectPaths() {
	}

	/**
	 * Parse a raw select string into ordered unique paths.
	 *
	 * @return empty list when blank/null (means “return full default shape”)
	 */
	public static List<String> parse(String raw) {
		if (raw == null || raw.isBlank()) {
			return List.of();
		}
		List<String> paths = Arrays.stream(raw.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.distinct()
				.toList();
		if (paths.size() > MAX_PATHS) {
			throw new IllegalArgumentException("select may contain at most " + MAX_PATHS + " paths");
		}
		for (String path : paths) {
			validatePathSyntax(path);
		}
		return paths;
	}

	/**
	 * Ensure every path's root segment is allow-listed for the resource.
	 *
	 * @param allowedRoots top-level field names (case-sensitive as returned in JSON), e.g. id, name, owner
	 */
	public static void validateRoots(List<String> paths, Set<String> allowedRoots) {
		if (paths.isEmpty()) {
			return;
		}
		if (allowedRoots == null || allowedRoots.isEmpty()) {
			throw new IllegalArgumentException("allowed select roots must not be empty");
		}
		Set<String> allowedLower = allowedRoots.stream()
				.map(r -> r.toLowerCase(Locale.ROOT))
				.collect(Collectors.toSet());
		var canonical = allowedRoots.stream()
				.collect(Collectors.toMap(r -> r.toLowerCase(Locale.ROOT), r -> r, (a, b) -> a));

		for (String path : paths) {
			String root = rootOf(path);
			String key = root.toLowerCase(Locale.ROOT);
			if (!allowedLower.contains(key)) {
				throw new IllegalArgumentException(
						"Invalid select field '" + root + "'; allowed: " + String.join(", ", allowedRoots));
			}
			// normalize root casing is caller's concern; syntax already checked
			if (!canonical.get(key).equals(root) && !allowedRoots.contains(root)) {
				// accept case-insensitive root match
			}
		}
	}

	/** True when any path is exactly {@code root} or starts with {@code root.}. */
	public static boolean requestsRoot(List<String> paths, String root) {
		if (paths.isEmpty()) {
			return false;
		}
		String prefix = root + ".";
		for (String path : paths) {
			if (path.equals(root) || path.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	/** Distinct root segments from paths. */
	public static Set<String> roots(List<String> paths) {
		Set<String> roots = new LinkedHashSet<>();
		for (String path : paths) {
			roots.add(rootOf(path));
		}
		return roots;
	}

	public static String rootOf(String path) {
		int dot = path.indexOf('.');
		return dot < 0 ? path : path.substring(0, dot);
	}

	private static void validatePathSyntax(String path) {
		if (path.length() > MAX_PATH_LENGTH) {
			throw new IllegalArgumentException("select path too long: " + path);
		}
		if (path.startsWith(".") || path.endsWith(".") || path.contains("..")) {
			throw new IllegalArgumentException("Invalid select path syntax: " + path);
		}
		String[] segments = path.split("\\.");
		if (segments.length > MAX_DEPTH) {
			throw new IllegalArgumentException(
					"select path depth exceeds " + MAX_DEPTH + ": " + path);
		}
		for (String segment : segments) {
			if (segment.isEmpty() || !segment.matches("[A-Za-z][A-Za-z0-9_]*")) {
				throw new IllegalArgumentException("Invalid select path segment in: " + path);
			}
		}
	}
}
