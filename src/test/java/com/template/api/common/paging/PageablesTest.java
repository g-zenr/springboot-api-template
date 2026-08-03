package com.template.api.common.paging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageablesTest {

	private static final Set<String> ALLOWED = Set.of("name", "createdAt", "updatedAt");
	private static final Sort DEFAULT = Sort.by(Sort.Direction.ASC, "name");

	@Test
	void toPageable_defaults_whenParamsOmitted() {
		PageQuery query = PageQuery.of(null, null, null, null);
		Pageable pageable = Pageables.toPageable(query, ALLOWED, DEFAULT);

		assertThat(pageable.getPageNumber()).isZero();
		assertThat(pageable.getPageSize()).isEqualTo(PageQuery.DEFAULT_LIMIT);
		assertThat(pageable.getSort()).isEqualTo(DEFAULT);
	}

	@Test
	void toPageable_appliesPageAndLimit() {
		PageQuery query = PageQuery.of(2, 5, null, null);
		Pageable pageable = Pageables.toPageable(query, ALLOWED, DEFAULT);

		assertThat(pageable.getPageNumber()).isEqualTo(2);
		assertThat(pageable.getPageSize()).isEqualTo(5);
	}

	@Test
	void toPageable_parsesSortAscAndDesc() {
		Pageable asc = Pageables.toPageable(PageQuery.of(0, 10, "name,asc", null), ALLOWED, DEFAULT);
		assertThat(asc.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);

		Pageable desc = Pageables.toPageable(PageQuery.of(0, 10, "createdAt,desc", null), ALLOWED, DEFAULT);
		assertThat(desc.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
	}

	@Test
	void toPageable_parsesMinusPrefixAsDesc() {
		Pageable pageable = Pageables.toPageable(PageQuery.of(0, 10, "-updatedAt", null), ALLOWED, DEFAULT);
		assertThat(pageable.getSort().getOrderFor("updatedAt").getDirection()).isEqualTo(Sort.Direction.DESC);
	}

	@Test
	void toPageable_parsesMultiFieldSort() {
		Pageable pageable =
				Pageables.toPageable(PageQuery.of(0, 10, "name,asc;createdAt,desc", null), ALLOWED, DEFAULT);
		assertThat(pageable.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
		assertThat(pageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
	}

	@Test
	void toPageable_rejectsInvalidSortField() {
		assertThatThrownBy(() -> Pageables.toPageable(PageQuery.of(0, 10, "hacked", null), ALLOWED, DEFAULT))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid sort field");
	}

	@Test
	void toPageable_rejectsNegativePage() {
		assertThatThrownBy(() -> Pageables.toPageable(PageQuery.of(-1, 10, null, null), ALLOWED, DEFAULT))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("page");
	}

	@Test
	void toPageable_rejectsLimitOutOfRange() {
		assertThatThrownBy(() -> Pageables.toPageable(PageQuery.of(0, 0, null, null), ALLOWED, DEFAULT))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("limit");

		assertThatThrownBy(
						() -> Pageables.toPageable(PageQuery.of(0, PageQuery.MAX_LIMIT + 1, null, null), ALLOWED, DEFAULT))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("limit");
	}

	@Test
	void normalizedSearch_trimsAndIgnoresBlank() {
		assertThat(PageQuery.of(0, 10, null, "  widget  ").normalizedSearch()).contains("widget");
		assertThat(PageQuery.of(0, 10, null, "   ").normalizedSearch()).isEmpty();
		assertThat(PageQuery.of(0, 10, null, null).normalizedSearch()).isEmpty();
	}
}
