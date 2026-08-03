package com.template.api.config.security;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Fails fast when JWT secret is unsafe outside the local profile.
 */
@Component
@Order(0)
public class JwtSecretValidator implements ApplicationRunner {

	static final String INSECURE_DEFAULT = "change-me-template-jwt-secret-key-32b!";

	private static final Logger log = LoggerFactory.getLogger(JwtSecretValidator.class);

	private final JwtProperties jwtProperties;
	private final Environment environment;

	public JwtSecretValidator(JwtProperties jwtProperties, Environment environment) {
		this.jwtProperties = jwtProperties;
		this.environment = environment;
	}

	@Override
	public void run(ApplicationArguments args) {
		String secret = jwtProperties.secret();
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("app.security.jwt.secret / JWT_SECRET must be set");
		}
		if (secret.length() < 32) {
			throw new IllegalStateException("JWT secret must be at least 32 characters");
		}

		boolean localOnly = Arrays.asList(environment.getActiveProfiles()).contains("local")
				|| (environment.getActiveProfiles().length == 0
						&& "local".equals(environment.getProperty("spring.profiles.default", "local")));

		// When default profile is local via spring.profiles.default
		if (environment.getActiveProfiles().length == 0) {
			localOnly = true;
		}
		else {
			localOnly = Arrays.asList(environment.getActiveProfiles()).contains("local")
					&& !Arrays.asList(environment.getActiveProfiles()).contains("prod");
		}

		if (!localOnly && INSECURE_DEFAULT.equals(secret)) {
			throw new IllegalStateException(
					"Insecure default JWT secret is not allowed outside the local profile. Set JWT_SECRET.");
		}

		if (INSECURE_DEFAULT.equals(secret)) {
			log.warn("Using insecure default JWT secret (acceptable for local template only)");
		}
	}
}
