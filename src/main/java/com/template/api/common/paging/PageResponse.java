package com.template.api.common.paging;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Reusable paged list response for any feature list endpoint.
 *
 * @param <T> element type (DTO, or {@code Map} when {@code select} projection is applied)
 */
@Schema(name = "PageResponse", description = "Paginated list envelope")
public record PageResponse<T>(
		@Schema(description = "Page of results (full DTO or sparse map when select is set)") List<T> content,
		@Schema(description = "Zero-based page index") int page,
		@Schema(description = "Requested page size") int limit,
		@Schema(description = "Total matching elements (-1 when count was not included/computed)") long totalElements,
		@Schema(description = "Total pages (-1 when count was not computed)") int totalPages,
		@Schema(description = "True when this is the first page") boolean first,
		@Schema(description = "True when this is the last page") boolean last,
		@Schema(description = "True when a next page exists") boolean hasNext,
		@Schema(description = "True when a previous page exists") boolean hasPrevious,
		@Schema(description = "Sort expression applied (may be null)") String sort,
		@Schema(description = "Search term applied (may be null)") String search,
		@Schema(description = "Select expression applied (may be null)") String select,
		@Schema(description = "Include expression applied (may be null)") String include) {

	/**
	 * Build from a Spring Data {@link Page} and the original query.
	 */
	public static <T> PageResponse<T> of(Page<T> result, PageQuery query) {
		return new PageResponse<>(
				result.getContent(),
				result.getNumber(),
				result.getSize(),
				result.getTotalElements(),
				result.getTotalPages(),
				result.isFirst(),
				result.isLast(),
				result.hasNext(),
				result.hasPrevious(),
				query.getSort(),
				query.normalizedSearch().orElse(null),
				blankToNull(query.getSelect()),
				blankToNull(query.getInclude()));
	}

	/**
	 * Build from pre-mapped content (used by {@link PagedReads}).
	 */
	public static <T> PageResponse<T> ofMapped(
			List<T> content,
			Pageable pageable,
			long totalElements,
			int totalPages,
			boolean first,
			boolean last,
			boolean hasNext,
			boolean hasPrevious,
			PageQuery query) {
		return new PageResponse<>(
				content,
				pageable.getPageNumber(),
				pageable.getPageSize(),
				totalElements,
				totalPages,
				first,
				last,
				hasNext,
				hasPrevious,
				query.getSort(),
				query.normalizedSearch().orElse(null),
				blankToNull(query.getSelect()),
				blankToNull(query.getInclude()));
	}

	/**
	 * Map entity page to DTO page.
	 */
	public static <E, T> PageResponse<T> map(Page<E> result, PageQuery query, Function<E, T> mapper) {
		return of(result.map(mapper), query);
	}

	/**
	 * Replace content while preserving page metadata (used after field selection).
	 */
	public <R> PageResponse<R> withContent(List<R> newContent) {
		return new PageResponse<>(
				newContent,
				page,
				limit,
				totalElements,
				totalPages,
				first,
				last,
				hasNext,
				hasPrevious,
				sort,
				search,
				select,
				include);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
