package com.template.api.config.security.authz;

/**
 * Canonical action strings for {@link AuthorizationService}.
 * Extend as new protected operations are added.
 */
public final class AuthzActions {

	public static final String USER_LIST = "user:list";
	public static final String USER_CREATE = "user:create";

	public static final String ITEM_LIST = "item:list";
	public static final String ITEM_READ = "item:read";
	public static final String ITEM_CREATE = "item:create";
	public static final String ITEM_UPDATE = "item:update";
	public static final String ITEM_DELETE = "item:delete";

	public static final String NOTE_LIST = "note:list";
	public static final String NOTE_READ = "note:read";
	public static final String NOTE_CREATE = "note:create";
	public static final String NOTE_UPDATE = "note:update";
	public static final String NOTE_DELETE = "note:delete";

	private AuthzActions() {
	}
}
