package com.template.api.config.security.authz;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Config-switchable authorization entry point for method security and services.
 *
 * <p>Bean name {@code authz} is used in {@code @PreAuthorize}:
 * <pre>
 * {@literal @}PreAuthorize("@authz.allow(authentication, 'user:list')")
 * </pre>
 *
 * <p>Mode: {@code app.security.authorization.mode=rbac|abac} (default {@code rbac}).
 */
@Component("authz")
public class AuthorizationService {

	private final AuthorizationProperties properties;
	private final RbacEvaluator rbacEvaluator;
	private final AbacEvaluator abacEvaluator;

	public AuthorizationService(
			AuthorizationProperties properties,
			RbacEvaluator rbacEvaluator,
			AbacEvaluator abacEvaluator) {
		this.properties = properties;
		this.rbacEvaluator = rbacEvaluator;
		this.abacEvaluator = abacEvaluator;
	}

	/** Current mode (for diagnostics / OpenAPI narrative). */
	public AuthorizationMode mode() {
		return properties.mode();
	}

	/**
	 * Whether the principal may perform {@code action} (no resource attributes).
	 */
	public boolean allow(Authentication authentication, String action) {
		return allow(authentication, action, null);
	}

	/**
	 * Whether the principal may perform {@code action} on optional {@code resource}
	 * (e.g. a {@link com.template.api.feature.note.Note} for ownership checks).
	 */
	public boolean allow(Authentication authentication, String action, Object resource) {
		return switch (properties.mode()) {
			case RBAC -> rbacEvaluator.allow(authentication, action, resource);
			case ABAC -> abacEvaluator.allow(authentication, action, resource);
		};
	}

	/**
	 * Same as {@link #allow} but throws {@link AccessDeniedException} on deny.
	 */
	public void require(Authentication authentication, String action, Object resource) {
		if (!allow(authentication, action, resource)) {
			throw new AccessDeniedException("Access denied for action: " + action);
		}
	}
}
