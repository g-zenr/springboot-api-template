package com.template.api.common.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimitServiceTest {

	private RateLimitService service;

	@BeforeEach
	void setUp() {
		service = new RateLimitService();
	}

	@Test
	void allowsUpToMaxRequests() {
		assertTrue(service.tryConsume("k1", 3, 60_000));
		assertTrue(service.tryConsume("k1", 3, 60_000));
		assertTrue(service.tryConsume("k1", 3, 60_000));
	}

	@Test
	void rejectsOverMax() {
		service.tryConsume("k2", 2, 60_000);
		service.tryConsume("k2", 2, 60_000);
		assertFalse(service.tryConsume("k2", 2, 60_000));
	}

	@Test
	void separateKeysAreIndependent() {
		assertTrue(service.tryConsume("a", 1, 60_000));
		assertTrue(service.tryConsume("b", 1, 60_000));
		assertFalse(service.tryConsume("a", 1, 60_000));
	}

	@Test
	void clear_resetsWindows() {
		service.tryConsume("k3", 1, 60_000);
		assertFalse(service.tryConsume("k3", 1, 60_000));
		service.clear();
		assertTrue(service.tryConsume("k3", 1, 60_000));
	}

	@Test
	void zeroMax_alwaysRejects() {
		assertFalse(service.tryConsume("z", 0, 60_000));
	}
}
