package com.template.api.common.paging;

import java.util.Optional;

import com.template.api.common.include.IncludeOptions;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Reusable list query parameters for any feature list endpoint.
 *
 * <p>Bind from the query string via Spring MVC property binding.
 *
 * <p>Example:
 * {@code GET /api/v1/items?page=0&limit=20&sort=name,asc&search=widget&select=id,name&include=count}
 */
@Schema(
		name = "PageQuery",
		description = "Reusable list query: page, limit, sort, search, select, include")
public class PageQuery {

	public static final int DEFAULT_PAGE = 0;
	public static final int DEFAULT_LIMIT = 20;
	public static final int MAX_LIMIT = 100;

	@Schema(description = "Zero-based page index", example = "0", defaultValue = "0")
	private int page = DEFAULT_PAGE;

	@Schema(description = "Max items per page (1–100)", example = "20", defaultValue = "20")
	private int limit = DEFAULT_LIMIT;

	@Schema(
			description = "Sort as field[,direction]. Direction is asc or desc (default asc). "
					+ "Prefix with '-' for desc (e.g. -updatedAt). Multiple fields: name,asc;createdAt,desc",
			example = "name,asc")
	private String sort;

	@Schema(description = "Case-insensitive text search (fields depend on the resource)", example = "widget")
	private String search;

	@Schema(
			description = "Sparse fieldset: comma-separated paths to include in each row. "
					+ "Supports deep paths with dots (e.g. id,name,owner.username). "
					+ "Omit to return the full default DTO shape.",
			example = "id,name,owner.username")
	private String select;

	@Schema(
			description = "Optional extras (comma-separated, case-insensitive; unknown ignored). "
					+ "Available values: "
					+ "count (documents + totalElements), "
					+ "countOnly ({count} only — no rows), "
					+ "pagination (reserved), "
					+ "summary (reserved), "
					+ "statistics (reserved), "
					+ "relations (reserved), "
					+ "permissions (reserved), "
					+ "analytics (reserved). "
					+ "Omit for default list (content + totals). "
					+ "See docs/LIST-QUERY.md.",
			example = "countOnly")
	private String include;

	public PageQuery() {
	}

	public PageQuery(int page, int limit, String sort, String search, String select, String include) {
		this.page = page;
		this.limit = limit;
		this.sort = sort;
		this.search = search;
		this.select = select;
		this.include = include;
	}

	/** Factory for tests and non-HTTP call sites. */
	public static PageQuery of(Integer page, Integer limit, String sort, String search) {
		return of(page, limit, sort, search, null, null);
	}

	public static PageQuery of(Integer page, Integer limit, String sort, String search, String select) {
		return of(page, limit, sort, search, select, null);
	}

	public static PageQuery of(
			Integer page, Integer limit, String sort, String search, String select, String include) {
		return new PageQuery(
				page == null ? DEFAULT_PAGE : page,
				limit == null ? DEFAULT_LIMIT : limit,
				sort,
				search,
				select,
				include);
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getSelect() {
		return select;
	}

	public void setSelect(String select) {
		this.select = select;
	}

	public String getInclude() {
		return include;
	}

	public void setInclude(String include) {
		this.include = include;
	}

	/** Parsed include flags (unknown tokens already dropped). */
	public IncludeOptions includes() {
		return IncludeOptions.parse(include);
	}

	/**
	 * Normalized search term, or empty when blank/null.
	 */
	public Optional<String> normalizedSearch() {
		if (search == null || search.isBlank()) {
			return Optional.empty();
		}
		return Optional.of(search.trim());
	}

	/**
	 * Validates page/limit bounds. Called by {@link Pageables}.
	 *
	 * @throws IllegalArgumentException when out of range
	 */
	public void validate() {
		if (page < 0) {
			throw new IllegalArgumentException("page must be >= 0");
		}
		if (limit < 1 || limit > MAX_LIMIT) {
			throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
		}
	}
}
