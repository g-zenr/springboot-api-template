package com.template.api.config.security.authz;

/**
 * Switchable authorization model.
 *
 * <ul>
 *   <li>{@link #RBAC} — role-based (e.g. ADMIN for user admin APIs)</li>
 *   <li>{@link #ABAC} — attribute-based policies (subject + action + resource attributes)</li>
 * </ul>
 *
 * Config: {@code app.security.authorization.mode=rbac|abac}
 */
public enum AuthorizationMode {
	RBAC,
	ABAC
}
