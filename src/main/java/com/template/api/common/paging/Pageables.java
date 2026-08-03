package com.template.api.common.paging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Builds Spring {@link Pageable} from reusable {@link PageQuery} with an allow-listed sort.
 *
 * <p>Use from any feature service:
 * <pre>{@code
 * Pageable pageable = Pageables.toPageable(query, Set.of("name", "createdAt"), Sort.by("name"));
 * }</pre>
 */
public final class Pageables {

	private Pageables() {
	}

	/**
	 * Convert query params to a {@link Pageable}.
	 *
	 * @param query             page/limit/sort/search
	 * @param allowedSortFields entity property names that may appear in {@code sort}
	 * @param defaultSort       used when sort is blank
	 * @throws IllegalArgumentException for invalid page, limit, or sort field
	 */
	public static Pageable toPageable(PageQuery query, Set<String> allowedSortFields, Sort defaultSort) {
		if (query == null) {
			throw new IllegalArgumentException("query must not be null");
		}
		if (allowedSortFields == null || allowedSortFields.isEmpty()) {
			throw new IllegalArgumentException("allowedSortFields must not be empty");
		}
		if (defaultSort == null) {
			throw new IllegalArgumentException("defaultSort must not be null");
		}
		query.validate();
		Sort sort = parseSort(query.getSort(), allowedSortFields, defaultSort);
		return PageRequest.of(query.getPage(), query.getLimit(), sort);
	}

	/**
	 * Parse sort string into Spring {@link Sort}.
	 *
	 * <p>Supported forms:
	 * <ul>
	 *   <li>{@code name} — ascending</li>
	 *   <li>{@code name,asc} / {@code name,desc}</li>
	 *   <li>{@code -name} — descending</li>
	 *   <li>Multiple: {@code name,asc;createdAt,desc}</li>
	 * </ul>
	 */
	static Sort parseSort(String raw, Collection<String> allowedSortFields, Sort defaultSort) {
		if (raw == null || raw.isBlank()) {
			return defaultSort;
		}

		Set<String> allowedLower = allowedSortFields.stream()
				.map(f -> f.toLowerCase(Locale.ROOT))
				.collect(Collectors.toSet());
		// map lower → canonical name from allowlist
		var canonicalByLower = allowedSortFields.stream()
				.collect(Collectors.toMap(f -> f.toLowerCase(Locale.ROOT), f -> f, (a, b) -> a));

		List<Sort.Order> orders = new ArrayList<>();
		String[] segments = raw.split(";");
		for (String segment : segments) {
			String part = segment.trim();
			if (part.isEmpty()) {
				continue;
			}
			orders.add(parseSingleOrder(part, allowedLower, canonicalByLower));
		}
		if (orders.isEmpty()) {
			return defaultSort;
		}
		return Sort.by(orders);
	}

	private static Sort.Order parseSingleOrder(
			String part,
			Set<String> allowedLower,
			java.util.Map<String, String> canonicalByLower) {
		Sort.Direction direction = Sort.Direction.ASC;
		String field = part;

		if (field.startsWith("-")) {
			direction = Sort.Direction.DESC;
			field = field.substring(1).trim();
		}
		else if (field.contains(",")) {
			String[] bits = field.split(",", 2);
			field = bits[0].trim();
			String dir = bits[1].trim().toLowerCase(Locale.ROOT);
			if ("desc".equals(dir) || "descending".equals(dir)) {
				direction = Sort.Direction.DESC;
			}
			else if ("asc".equals(dir) || "ascending".equals(dir) || dir.isEmpty()) {
				direction = Sort.Direction.ASC;
			}
			else {
				throw new IllegalArgumentException(
						"Invalid sort direction '" + bits[1].trim() + "'; use asc or desc");
			}
		}

		if (field.isEmpty()) {
			throw new IllegalArgumentException("sort field must not be blank");
		}

		String key = field.toLowerCase(Locale.ROOT);
		if (!allowedLower.contains(key)) {
			throw new IllegalArgumentException(
					"Invalid sort field '" + field + "'; allowed: " + String.join(", ", allowedLower));
		}
		return new Sort.Order(direction, canonicalByLower.get(key));
	}
}
