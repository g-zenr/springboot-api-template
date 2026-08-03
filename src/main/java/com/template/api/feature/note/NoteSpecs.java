package com.template.api.feature.note;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable JPA specifications for note list/search (owner-scoped).
 */
final class NoteSpecs {

	private NoteSpecs() {
	}

	static Specification<Note> ownedBy(String username) {
		return (root, query, cb) -> cb.equal(root.get("ownerUsername"), username);
	}

	static Specification<Note> matchesSearch(String term) {
		String pattern = "%" + term.toLowerCase(Locale.ROOT) + "%";
		return (root, query, cb) -> cb.or(
				cb.like(cb.lower(root.get("title")), pattern),
				cb.like(cb.lower(cb.coalesce(root.get("body"), "")), pattern));
	}
}
