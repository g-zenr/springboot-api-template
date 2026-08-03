package com.template.api.config.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final JwtProperties properties;
	private final SecretKey key;

	public JwtService(JwtProperties properties) {
		this.properties = properties;
		this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateAccessToken(String username, List<String> roles) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + properties.expirationMs());
		return Jwts.builder()
				.subject(username)
				.claim("roles", roles)
				.claim("typ", "access")
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key)
				.compact();
	}

	public String extractUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public List<String> extractRoles(String token) {
		Object roles = parseClaims(token).get("roles");
		if (roles instanceof List<?> list) {
			return list.stream().map(Object::toString).toList();
		}
		return List.of();
	}

	public boolean isValid(String token, String expectedUsername) {
		Claims claims = parseClaims(token);
		return expectedUsername.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
	}

	public long expirationMs() {
		return properties.expirationMs();
	}

	public long refreshExpirationMs() {
		return properties.refreshExpirationMs();
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
