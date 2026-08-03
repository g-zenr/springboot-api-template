package com.template.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class CorsPropertiesTest {

	@Test
	void allowedOriginsList_splitsCommaSeparatedValues() {
		CorsProperties props = new CorsProperties("https://a.com, https://b.com", "GET", "*", false);
		assertEquals(List.of("https://a.com", "https://b.com"), props.allowedOriginsList());
	}

	@Test
	void allowedOriginsList_singleWildcard() {
		CorsProperties props = new CorsProperties("*", "GET", "*", false);
		assertEquals(List.of("*"), props.allowedOriginsList());
	}

	@Test
	void allowedOriginsList_blank_returnsEmpty() {
		CorsProperties props = new CorsProperties("  ", "GET", "*", false);
		assertTrue(props.allowedOriginsList().isEmpty());
	}

	@Test
	void allowedMethodsList_splitsMethods() {
		CorsProperties props = new CorsProperties("*", "GET,POST,PUT", "*", false);
		assertEquals(List.of("GET", "POST", "PUT"), props.allowedMethodsList());
	}

	@Test
	void allowedHeadersList_andCredentialsFlag() {
		CorsProperties props = new CorsProperties("*", "GET", "Authorization,Content-Type", true);
		assertEquals(List.of("Authorization", "Content-Type"), props.allowedHeadersList());
		assertTrue(props.allowCredentials());
		assertFalse(new CorsProperties("*", "GET", "*", false).allowCredentials());
	}
}
