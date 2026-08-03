package com.template.api.config.security.authz;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Authorization model switch.
 *
 * <pre>
 * app.security.authorization.mode: rbac   # default
 * app.security.authorization.mode: abac
 * </pre>
 */
@ConfigurationProperties(prefix = "app.security.authorization")
public record AuthorizationProperties(AuthorizationMode mode) {

	public AuthorizationProperties {
		if (mode == null) {
			mode = AuthorizationMode.RBAC;
		}
	}
}
