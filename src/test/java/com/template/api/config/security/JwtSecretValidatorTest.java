package com.template.api.config.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;

class JwtSecretValidatorTest {

	@Test
	void local_allowsDefaultSecret() {
		MockEnvironment env = new MockEnvironment();
		env.setActiveProfiles("local");
		JwtSecretValidator validator = new JwtSecretValidator(
				new JwtProperties(JwtSecretValidator.INSECURE_DEFAULT, 3600000, 604800000),
				env);
		assertDoesNotThrow(() -> validator.run(new DefaultApplicationArguments()));
	}

	@Test
	void prod_rejectsDefaultSecret() {
		MockEnvironment env = new MockEnvironment();
		env.setActiveProfiles("prod");
		JwtSecretValidator validator = new JwtSecretValidator(
				new JwtProperties(JwtSecretValidator.INSECURE_DEFAULT, 3600000, 604800000),
				env);
		assertThrows(IllegalStateException.class, () -> validator.run(new DefaultApplicationArguments()));
	}

	@Test
	void rejectsShortSecret() {
		MockEnvironment env = new MockEnvironment();
		env.setActiveProfiles("local");
		JwtSecretValidator validator = new JwtSecretValidator(
				new JwtProperties("too-short", 3600000, 604800000),
				env);
		assertThrows(IllegalStateException.class, () -> validator.run(new DefaultApplicationArguments()));
	}

	@Test
	void prod_acceptsStrongSecret() {
		MockEnvironment env = new MockEnvironment();
		env.setActiveProfiles("prod");
		JwtSecretValidator validator = new JwtSecretValidator(
				new JwtProperties("this-is-a-strong-production-secret-key!!", 3600000, 604800000),
				env);
		assertDoesNotThrow(() -> validator.run(new DefaultApplicationArguments()));
	}

	@Test
	void blankSecret_rejected() {
		MockEnvironment env = new MockEnvironment();
		env.setActiveProfiles("local");
		JwtSecretValidator validator = new JwtSecretValidator(
				new JwtProperties("   ", 3600000, 604800000),
				env);
		assertThrows(IllegalStateException.class, () -> validator.run(new DefaultApplicationArguments()));
	}
}
