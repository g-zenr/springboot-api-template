package com.template.api.feature.user;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable JPA specifications for user list/search.
 */
final class UserSpecs {

	private UserSpecs() {
	}

	static Specification<AppUser> matchesSearch(String term) {
		String pattern = "%" + term.toLowerCase(Locale.ROOT) + "%";
		return (root, query, cb) -> cb.like(cb.lower(root.get("username")), pattern);
	}
}
