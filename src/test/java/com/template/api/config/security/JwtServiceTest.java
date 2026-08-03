package com.template.api.config.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService(new JwtProperties(
				"change-me-template-jwt-secret-key-32b!",
				3_600_000L,
				604_800_000L));
	}

	@Test
	void generateAccessToken_roundTripsUsername() {
		String token = jwtService.generateAccessToken("admin", List.of("ADMIN"));
		assertEquals("admin", jwtService.extractUsername(token));
	}

	@Test
	void generateAccessToken_roundTripsRoles() {
		String token = jwtService.generateAccessToken("admin", List.of("ADMIN", "USER"));
		assertEquals(List.of("ADMIN", "USER"), jwtService.extractRoles(token));
	}

	@Test
	void isValid_trueForMatchingUser() {
		String token = jwtService.generateAccessToken("user", List.of("USER"));
		assertTrue(jwtService.isValid(token, "user"));
	}

	@Test
	void isValid_falseForDifferentUser() {
		String token = jwtService.generateAccessToken("user", List.of("USER"));
		assertFalse(jwtService.isValid(token, "admin"));
	}

	@Test
	void parseClaims_rejectsTamperedToken() {
		String token = jwtService.generateAccessToken("admin", List.of("ADMIN"));
		String tampered = token.substring(0, token.length() - 4) + "xxxx";
		assertThrows(Exception.class, () -> jwtService.extractUsername(tampered));
	}

	@Test
	void expirationMs_andRefresh_matchConfig() {
		assertEquals(3_600_000L, jwtService.expirationMs());
		assertEquals(604_800_000L, jwtService.refreshExpirationMs());
	}
}
