package com.template.api.common.select;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.template.api.common.paging.PageQuery;
import com.template.api.common.paging.PageResponse;

/**
 * Applies reusable {@code select} projection to paged (or single) responses.
 *
 * <p>When {@code select} is omitted, the full DTO list is returned unchanged.
 * When set, each row becomes a sparse {@link Map} (supports deep paths via dots).
 */
public final class FieldSelection {

	private FieldSelection() {
	}

	/**
	 * Project a page of DTOs using the query's select paths and an allow-list of root fields.
	 *
	 * @return original page when select is blank; otherwise page of projected maps
	 */
	public static <T> PageResponse<?> apply(PageResponse<T> page, PageQuery query, Set<String> allowedRoots) {
		List<String> paths = SelectPaths.parse(query.getSelect());
		SelectPaths.validateRoots(paths, allowedRoots);
		if (paths.isEmpty()) {
			return page;
		}
		List<Map<String, Object>> projected = FieldProjector.projectAll(page.content(), paths);
		return page.withContent(projected);
	}

	/**
	 * Project after enriching each DTO (e.g. attach nested {@code owner} for deep select).
	 *
	 * @param enrich builds a map/DTO that includes nested objects to select from
	 */
	public static <T> PageResponse<?> applyEnriched(
			PageResponse<T> page,
			PageQuery query,
			Set<String> allowedRoots,
			Function<T, Object> enrich) {
		List<String> paths = SelectPaths.parse(query.getSelect());
		SelectPaths.validateRoots(paths, allowedRoots);
		if (paths.isEmpty()) {
			return page;
		}
		List<Map<String, Object>> projected = page.content().stream()
				.map(enrich)
				.map(src -> FieldProjector.project(src, paths))
				.toList();
		return page.withContent(projected);
	}

	/**
	 * Project a single resource (GET by id). Returns the original value when select is blank.
	 */
	public static Object applyOne(Object source, String select, Set<String> allowedRoots) {
		List<String> paths = SelectPaths.parse(select);
		SelectPaths.validateRoots(paths, allowedRoots);
		if (paths.isEmpty()) {
			return source;
		}
		return FieldProjector.project(source, paths);
	}
}
