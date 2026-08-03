package com.template.api.common.select;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Projects a source object (DTO, Map, or nested graph) into a map using select paths.
 *
 * <p>Deep paths use dots: {@code owner.username}, {@code owner.roles}, etc.
 * Missing intermediate values are omitted (not errors).
 *
 * <p>When select is empty, callers should skip projection and return the full DTO.
 */
public final class FieldProjector {

	private static final ObjectMapper MAPPER = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};

	private FieldProjector() {
	}

	/**
	 * Convert any Jackson-friendly value to a plain map (for enrichment before project).
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(Object source) {
		if (source == null) {
			return new LinkedHashMap<>();
		}
		if (source instanceof Map<?, ?> map) {
			Map<String, Object> copy = new LinkedHashMap<>();
			map.forEach((k, v) -> copy.put(String.valueOf(k), normalizeValue(v)));
			return copy;
		}
		return MAPPER.convertValue(source, MAP_TYPE);
	}

	/**
	 * Build a sparse map containing only the requested paths.
	 */
	public static Map<String, Object> project(Object source, Collection<String> paths) {
		if (paths == null || paths.isEmpty()) {
			return toMap(source);
		}
		Map<String, Object> sourceMap = toMap(source);
		Map<String, Object> result = new LinkedHashMap<>();
		for (String path : paths) {
			Object value = readPath(sourceMap, path);
			if (value != null || pathExists(sourceMap, path)) {
				writePath(result, path, value);
			}
		}
		return result;
	}

	/**
	 * Project each element; empty paths returns empty list only if sources empty —
	 * callers use this when select is non-empty.
	 */
	public static List<Map<String, Object>> projectAll(Collection<?> sources, Collection<String> paths) {
		return sources.stream().map(s -> project(s, paths)).toList();
	}

	@SuppressWarnings("unchecked")
	static Object readPath(Map<String, Object> source, String path) {
		String[] segments = path.split("\\.");
		Object current = source;
		for (String segment : segments) {
			if (!(current instanceof Map<?, ?> map)) {
				return null;
			}
			current = map.get(segment);
			if (current == null) {
				return null;
			}
		}
		return normalizeValue(current);
	}

	@SuppressWarnings("unchecked")
	static boolean pathExists(Map<String, Object> source, String path) {
		String[] segments = path.split("\\.");
		Object current = source;
		for (int i = 0; i < segments.length; i++) {
			if (!(current instanceof Map<?, ?> map)) {
				return false;
			}
			if (!map.containsKey(segments[i])) {
				return false;
			}
			current = map.get(segments[i]);
			if (current == null && i < segments.length - 1) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	static void writePath(Map<String, Object> target, String path, Object value) {
		String[] segments = path.split("\\.");
		Map<String, Object> current = target;
		for (int i = 0; i < segments.length - 1; i++) {
			String segment = segments[i];
			Object next = current.get(segment);
			if (!(next instanceof Map<?, ?>)) {
				Map<String, Object> nested = new LinkedHashMap<>();
				current.put(segment, nested);
				current = nested;
			}
			else {
				current = (Map<String, Object>) next;
			}
		}
		current.put(segments[segments.length - 1], value);
	}

	@SuppressWarnings("unchecked")
	private static Object normalizeValue(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Map<?, ?> || value instanceof Collection<?> || value.getClass().isArray()) {
			return MAPPER.convertValue(value, Object.class);
		}
		// records / beans already flattened via toMap; leaf Instant/UUID/String stay as-is after convert
		if (value instanceof Number || value instanceof Boolean || value instanceof String) {
			return value;
		}
		// UUID, Instant, enums → Jackson-friendly
		return MAPPER.convertValue(value, Object.class);
	}
}
