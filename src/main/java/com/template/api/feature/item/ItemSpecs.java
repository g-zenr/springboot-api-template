package com.template.api.feature.item;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable JPA specifications for item list/search.
 */
final class ItemSpecs {

	private ItemSpecs() {
	}

	static Specification<Item> matchesSearch(String term) {
		String pattern = "%" + term.toLowerCase(Locale.ROOT) + "%";
		return (root, query, cb) -> cb.or(
				cb.like(cb.lower(root.get("name")), pattern),
				cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern));
	}
}
