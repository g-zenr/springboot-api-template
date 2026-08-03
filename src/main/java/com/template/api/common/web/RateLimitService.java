package com.template.api.common.web;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Simple in-memory sliding-window rate limiter (single-node template).
 */
@Service
public class RateLimitService {

	private final Map<String, Deque<Long>> windows = new ConcurrentHashMap<>();

	public boolean tryConsume(String key, int maxRequests, long windowMs) {
		long now = Instant.now().toEpochMilli();
		long cutoff = now - windowMs;
		Deque<Long> deque = windows.computeIfAbsent(key, k -> new ArrayDeque<>());
		synchronized (deque) {
			while (!deque.isEmpty() && deque.peekFirst() < cutoff) {
				deque.pollFirst();
			}
			if (deque.size() >= maxRequests) {
				return false;
			}
			deque.addLast(now);
			return true;
		}
	}

	/** Test helper */
	void clear() {
		windows.clear();
	}
}
