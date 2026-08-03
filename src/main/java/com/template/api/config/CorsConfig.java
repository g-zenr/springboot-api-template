package com.template.api.config;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

	@Bean
	CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
		CorsConfiguration config = new CorsConfiguration();
		List<String> origins = props.allowedOriginsList();
		if (origins.size() == 1 && "*".equals(origins.getFirst())) {
			config.addAllowedOriginPattern("*");
		}
		else {
			config.setAllowedOrigins(origins);
		}
		config.setAllowedMethods(props.allowedMethodsList());
		config.setAllowedHeaders(props.allowedHeadersList());
		config.setAllowCredentials(props.allowCredentials());
		config.setMaxAge(3600L);
		// Allow clients to read correlation id from responses
		config.addExposedHeader("X-Correlation-Id");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
