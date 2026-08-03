package com.template.api.config.security.authz;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.template.api.feature.note.Note;

class AuthorizationServiceTest {

	private static AuthorizationService service(AuthorizationMode mode) {
		return new AuthorizationService(
				new AuthorizationProperties(mode),
				new RbacEvaluator(),
				new AbacEvaluator());
	}

	private static UsernamePasswordAuthenticationToken user(String name, String... roles) {
		List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
				.map(r -> new SimpleGrantedAuthority("ROLE_" + r))
				.toList();
		return new UsernamePasswordAuthenticationToken(name, "n/a", authorities);
	}

	@Test
	void rbac_adminCanListUsers_userCannot() {
		AuthorizationService authz = service(AuthorizationMode.RBAC);
		assertThat(authz.allow(user("admin", "ADMIN"), AuthzActions.USER_LIST)).isTrue();
		assertThat(authz.allow(user("user", "USER"), AuthzActions.USER_LIST)).isFalse();
	}

	@Test
	void abac_adminCanListUsers_userCannot() {
		AuthorizationService authz = service(AuthorizationMode.ABAC);
		assertThat(authz.allow(user("admin", "ADMIN"), AuthzActions.USER_LIST)).isTrue();
		assertThat(authz.allow(user("user", "USER"), AuthzActions.USER_LIST)).isFalse();
	}

	@Test
	void rbac_noteAccess_ownerOnly() {
		AuthorizationService authz = service(AuthorizationMode.RBAC);
		Note note = new Note("alice", "t", "b");
		assertThat(authz.allow(user("alice", "USER"), AuthzActions.NOTE_READ, note)).isTrue();
		assertThat(authz.allow(user("bob", "USER"), AuthzActions.NOTE_READ, note)).isFalse();
		assertThat(authz.allow(user("admin", "ADMIN"), AuthzActions.NOTE_READ, note)).isFalse();
	}

	@Test
	void abac_noteAccess_ownerOrAdmin() {
		AuthorizationService authz = service(AuthorizationMode.ABAC);
		Note note = new Note("alice", "t", "b");
		assertThat(authz.allow(user("alice", "USER"), AuthzActions.NOTE_READ, note)).isTrue();
		assertThat(authz.allow(user("bob", "USER"), AuthzActions.NOTE_READ, note)).isFalse();
		// ABAC default policy: ADMIN may access any note (attribute/role policy)
		assertThat(authz.allow(user("admin", "ADMIN"), AuthzActions.NOTE_READ, note)).isTrue();
	}

	@Test
	void bothModes_authenticatedCanUseItems() {
		for (AuthorizationMode mode : AuthorizationMode.values()) {
			AuthorizationService authz = service(mode);
			assertThat(authz.allow(user("user", "USER"), AuthzActions.ITEM_LIST)).isTrue();
			assertThat(authz.allow(user("user", "USER"), AuthzActions.ITEM_CREATE)).isTrue();
		}
	}

	@Test
	void mode_reportsConfiguredMode() {
		assertThat(service(AuthorizationMode.RBAC).mode()).isEqualTo(AuthorizationMode.RBAC);
		assertThat(service(AuthorizationMode.ABAC).mode()).isEqualTo(AuthorizationMode.ABAC);
	}
}
