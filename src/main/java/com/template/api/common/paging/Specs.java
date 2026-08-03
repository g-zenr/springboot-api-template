package com.template.api.common.paging;

import org.springframework.data.jpa.domain.Specification;

/**
 * Small helpers for composing JPA {@link Specification}s in feature services.
 */
public final class Specs {

	private Specs() {
	}

	/** Match all rows (no restriction). */
	public static <T> Specification<T> unrestricted() {
		return (root, query, cb) -> cb.conjunction();
	}
}
