package com.template.api.common.web;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitFilter extends OncePerRequestFilter {

	private static final long WINDOW_MS = 60_000L;

	private final RateLimitService rateLimitService;
	private final RateLimitProperties properties;

	public RateLimitFilter(RateLimitService rateLimitService, RateLimitProperties properties) {
		this.rateLimitService = rateLimitService;
		this.properties = properties;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		if (!properties.enabled()) {
			filterChain.doFilter(request, response);
			return;
		}

		String path = request.getRequestURI();
		String client = clientKey(request);
		boolean allowed;
		if (path.startsWith("/api/v1/auth/login") && "POST".equalsIgnoreCase(request.getMethod())) {
			allowed = rateLimitService.tryConsume(
					"login:" + client,
					properties.loginRequestsPerMinute(),
					WINDOW_MS);
		}
		else if (path.startsWith("/api/")) {
			allowed = rateLimitService.tryConsume(
					"api:" + client,
					properties.apiRequestsPerMinute(),
					WINDOW_MS);
		}
		else {
			allowed = true;
		}

		if (!allowed) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.getWriter().write(
					"{\"type\":\"about:blank\",\"title\":\"Too Many Requests\",\"status\":429,"
							+ "\"detail\":\"Rate limit exceeded\"}");
			return;
		}

		filterChain.doFilter(request, response);
	}

	private static String clientKey(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
	}
}
