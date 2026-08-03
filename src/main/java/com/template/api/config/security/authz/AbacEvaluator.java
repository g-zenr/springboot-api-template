package com.template.api.config.security.authz;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.template.api.feature.note.Note;

/**
 * Attribute-based evaluation using simple default policies (template sample).
 *
 * <p>Subject attributes: {@code username}, {@code roles}.
 * Resource attributes (when present): {@code type}, {@code ownerUsername}.
 * Action: string like {@code note:read}.
 *
 * <p>Default policies mirror the template’s product rules (admin user APIs, owner-scoped notes,
 * authenticated items). Extend {@link #evaluatePolicies} for richer ABAC without changing RBAC.
 */
@Component
class AbacEvaluator {

	boolean allow(Authentication authentication, String action, Object resource) {
		if (authentication == null || !authentication.isAuthenticated() || action == null || action.isBlank()) {
			return false;
		}
		Map<String, Object> subject = subjectAttrs(authentication);
		Map<String, Object> res = resourceAttrs(resource);
		return evaluatePolicies(subject, action, res);
	}

	/**
	 * Default policy set — keep aligned with RBAC defaults so mode switch is safe out of the box.
	 */
	private boolean evaluatePolicies(Map<String, Object> subject, String action, Map<String, Object> resource) {
		Set<String> roles = rolesOf(subject);

		// Admin-only user management
		if (AuthzActions.USER_LIST.equals(action) || AuthzActions.USER_CREATE.equals(action)) {
			return roles.contains("ADMIN");
		}

		// Items: any authenticated subject
		if (action.startsWith("item:")) {
			return true;
		}

		// Notes list/create: authenticated
		if (AuthzActions.NOTE_LIST.equals(action) || AuthzActions.NOTE_CREATE.equals(action)) {
			return true;
		}

		// Notes read/update/delete: owner match (attribute) OR admin role
		if (AuthzActions.NOTE_READ.equals(action)
				|| AuthzActions.NOTE_UPDATE.equals(action)
				|| AuthzActions.NOTE_DELETE.equals(action)) {
			if (roles.contains("ADMIN")) {
				return true;
			}
			String owner = stringAttr(resource, "ownerUsername");
			String username = stringAttr(subject, "username");
			return owner != null && username != null && owner.equalsIgnoreCase(username);
		}

		return false;
	}

	private static Map<String, Object> subjectAttrs(Authentication authentication) {
		Map<String, Object> attrs = new HashMap<>();
		attrs.put("username", authentication.getName());
		attrs.put("roles", authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
				.map(r -> r.toUpperCase(Locale.ROOT))
				.collect(java.util.stream.Collectors.toUnmodifiableSet()));
		return attrs;
	}

	private static Map<String, Object> resourceAttrs(Object resource) {
		Map<String, Object> attrs = new HashMap<>();
		if (resource instanceof Note note) {
			attrs.put("type", "note");
			attrs.put("ownerUsername", note.getOwnerUsername());
			attrs.put("id", note.getId() != null ? note.getId().toString() : null);
		}
		else if (resource instanceof Map<?, ?> map) {
			map.forEach((k, v) -> attrs.put(String.valueOf(k), v));
		}
		return attrs;
	}

	@SuppressWarnings("unchecked")
	private static Set<String> rolesOf(Map<String, Object> subject) {
		Object roles = subject.get("roles");
		if (roles instanceof Set<?> set) {
			return (Set<String>) set;
		}
		return Set.of();
	}

	private static String stringAttr(Map<String, Object> map, String key) {
		Object value = map.get(key);
		return value == null ? null : String.valueOf(value);
	}
}
