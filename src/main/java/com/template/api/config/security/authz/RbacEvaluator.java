package com.template.api.config.security.authz;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.template.api.feature.note.Note;

/**
 * Role-based checks. Admin APIs need {@code ROLE_ADMIN}; notes require owner match when a resource is provided.
 */
@Component
class RbacEvaluator {

	boolean allow(Authentication authentication, String action, Object resource) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}
		return switch (action) {
			case AuthzActions.USER_LIST, AuthzActions.USER_CREATE -> hasRole(authentication, "ADMIN");
			case AuthzActions.ITEM_LIST, AuthzActions.ITEM_READ, AuthzActions.ITEM_CREATE,
					AuthzActions.ITEM_UPDATE, AuthzActions.ITEM_DELETE -> true;
			case AuthzActions.NOTE_LIST, AuthzActions.NOTE_CREATE -> true;
			case AuthzActions.NOTE_READ, AuthzActions.NOTE_UPDATE, AuthzActions.NOTE_DELETE ->
					isNoteOwner(authentication, resource);
			default -> false;
		};
	}

	private static boolean isNoteOwner(Authentication authentication, Object resource) {
		if (!(resource instanceof Note note)) {
			// List/create paths pass null resource; deny unknown resource shapes for note mutations.
			return false;
		}
		return authentication.getName() != null
				&& authentication.getName().equalsIgnoreCase(note.getOwnerUsername());
	}

	private static boolean hasRole(Authentication authentication, String role) {
		String expected = "ROLE_" + role;
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (expected.equals(authority.getAuthority())) {
				return true;
			}
		}
		return false;
	}
}
