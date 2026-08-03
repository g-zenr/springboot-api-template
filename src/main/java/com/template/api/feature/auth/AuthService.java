package com.template.api.feature.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.template.api.common.error.ResourceNotFoundException;
import com.template.api.config.security.JwtService;
import com.template.api.feature.user.AppUser;
import com.template.api.feature.user.AppUserRepository;
import com.template.api.feature.user.RefreshToken;
import com.template.api.feature.user.RefreshTokenRepository;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final AppUserRepository appUserRepository;
	private final RefreshTokenRepository refreshTokenRepository;

	public AuthService(
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			AppUserRepository appUserRepository,
			RefreshTokenRepository refreshTokenRepository) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.appUserRepository = appUserRepository;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	@Transactional
	public TokenResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.username(), request.password()));

		List<String> roles = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
				.toList();

		AppUser user = appUserRepository.findByUsernameIgnoreCase(authentication.getName())
				.orElseThrow(() -> new ResourceNotFoundException("User not found after authentication"));

		return issueTokens(user, roles);
	}

	@Transactional
	public TokenResponse refresh(RefreshTokenRequest request) {
		String hash = sha256(request.refreshToken());
		RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
				.orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException(
						"Invalid refresh token"));

		if (!stored.isActive()) {
			throw new org.springframework.security.authentication.BadCredentialsException("Refresh token expired or revoked");
		}

		// Rotate: revoke current, issue new pair
		stored.revoke();
		refreshTokenRepository.save(stored);

		AppUser user = stored.getUser();
		List<String> roles = user.roleList();
		return issueTokens(user, roles);
	}

	@Transactional
	public void logout(RefreshTokenRequest request) {
		String hash = sha256(request.refreshToken());
		refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
			token.revoke();
			refreshTokenRepository.save(token);
		});
	}

	private TokenResponse issueTokens(AppUser user, List<String> roles) {
		String accessToken = jwtService.generateAccessToken(user.getUsername(), roles);
		String rawRefresh = UUID.randomUUID() + "." + UUID.randomUUID();
		Instant refreshExpiry = Instant.now().plusMillis(jwtService.refreshExpirationMs());
		refreshTokenRepository.save(new RefreshToken(user, sha256(rawRefresh), refreshExpiry));

		return new TokenResponse(
				accessToken,
				rawRefresh,
				"Bearer",
				jwtService.expirationMs(),
				jwtService.refreshExpirationMs(),
				user.getUsername(),
				roles);
	}

	static String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
