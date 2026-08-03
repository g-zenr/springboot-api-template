package com.template.api.feature.user;

import java.net.URI;
import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.template.api.common.paging.PageQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profile and admin provisioning (Bearer JWT)")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	@Operation(
			summary = "Current authenticated user",
			description = "Optional select=id,username,roles for sparse fieldset")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Current user profile"),
			@ApiResponse(responseCode = "400", description = "Invalid select"),
			@ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token")
	})
	public Object me(
			Authentication authentication,
			@RequestParam(required = false) String select) {
		return userService.getByUsername(authentication.getName(), select);
	}

	@PutMapping("/me/password")
	@Operation(summary = "Change current user password")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Password updated"),
			@ApiResponse(responseCode = "400", description = "Validation failed"),
			@ApiResponse(responseCode = "401", description = "Wrong current password or unauthenticated")
	})
	public ResponseEntity<Void> changePassword(
			Authentication authentication,
			@Valid @RequestBody ChangePasswordRequest request) {
		userService.changePassword(authentication.getName(), request);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	@PreAuthorize("@authz.allow(authentication, 'user:list')")
	@Operation(
			summary = "List users (admin, paginated)",
			description = "Requires admin authorization (RBAC role or ABAC policy — see app.security.authorization.mode). "
					+ "Query: page, limit, sort, search (username), select, include. "
					+ "Sort fields: username, enabled, createdAt, updatedAt. "
					+ "Select roots: id, username, roles, enabled, createdAt. "
					+ "include values: count (docs+totals), countOnly ({count} only), "
					+ "reserved: pagination|summary|statistics|relations|permissions|analytics. "
					+ "Omit include = default docs+totals.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Paged user list or CountResponse when include=countOnly"),
			@ApiResponse(responseCode = "400", description = "Invalid page, limit, sort, or select"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated"),
			@ApiResponse(responseCode = "403", description = "Not authorized (admin required under default policies)")
	})
	public Object list(@ParameterObject PageQuery query) {
		return userService.listAll(query);
	}

	@PostMapping
	@PreAuthorize("@authz.allow(authentication, 'user:create')")
	@Operation(
			summary = "Create user (admin)",
			description = "Requires admin authorization (RBAC/ABAC via app.security.authorization.mode); roles: ADMIN, USER")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "User created"),
			@ApiResponse(responseCode = "400", description = "Validation failed or unsupported role"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated"),
			@ApiResponse(responseCode = "403", description = "Not an admin"),
			@ApiResponse(responseCode = "409", description = "Username already exists")
	})
	public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
		UserResponse created = userService.createByAdmin(request);
		return ResponseEntity.created(URI.create("/api/v1/users/me")).body(created);
	}
}
