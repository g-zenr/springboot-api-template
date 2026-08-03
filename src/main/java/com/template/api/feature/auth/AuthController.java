package com.template.api.feature.auth;

import java.net.URI;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.template.api.feature.user.RegisterRequest;
import com.template.api.feature.user.UserResponse;
import com.template.api.feature.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Login, refresh, logout, register (public endpoints)")
public class AuthController {

	private final AuthService authService;
	private final UserService userService;

	public AuthController(AuthService authService, UserService userService) {
		this.authService = authService;
		this.userService = userService;
	}

	@PostMapping("/login")
	@SecurityRequirements
	@Operation(summary = "Login and receive access + refresh tokens")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Token pair issued"),
			@ApiResponse(responseCode = "400", description = "Validation failed"),
			@ApiResponse(responseCode = "401", description = "Invalid credentials"),
			@ApiResponse(responseCode = "429", description = "Rate limit exceeded")
	})
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/refresh")
	@SecurityRequirements
	@Operation(summary = "Rotate refresh token and issue a new token pair")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "New token pair issued; old refresh revoked"),
			@ApiResponse(responseCode = "400", description = "Validation failed"),
			@ApiResponse(responseCode = "401", description = "Invalid, expired, or revoked refresh token")
	})
	public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return authService.refresh(request);
	}

	@PostMapping("/logout")
	@SecurityRequirements
	@Operation(summary = "Revoke a refresh token")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Refresh token revoked (or already unknown)"),
			@ApiResponse(responseCode = "400", description = "Validation failed")
	})
	public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
		authService.logout(request);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/register")
	@SecurityRequirements
	@Operation(summary = "Self-register as USER", description = "Only when app.security.allow-registration=true")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "User created"),
			@ApiResponse(responseCode = "400", description = "Validation failed"),
			@ApiResponse(responseCode = "403", description = "Registration disabled"),
			@ApiResponse(responseCode = "409", description = "Username already exists")
	})
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		UserResponse created = userService.register(request);
		return ResponseEntity.created(URI.create("/api/v1/users/me")).body(created);
	}
}
