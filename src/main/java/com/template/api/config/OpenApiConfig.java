package com.template.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Global OpenAPI / Swagger metadata. Keep info.version and description in sync
 * with CHANGELOG / docs when the public narrative changes.
 *
 * @see docs/OPENAPI-SWAGGER.md
 */
@Configuration
public class OpenApiConfig {

	public static final String BEARER_SCHEME = "bearerAuth";

	@Bean
	OpenAPI apiTemplateOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Spring Boot API Template")
						.description(
								"""
										Reusable Spring Boot API starter (JPA, Flyway, JWT + refresh, users, items, notes).

										**Auth:** Obtain a token via POST /api/v1/auth/login, then click Authorize and enter: Bearer &lt;accessToken&gt;

										**Local demo users** (when seed-demo-users=true): admin/admin123, user/user123

										**Docs:** /docs/OPENAPI-SWAGGER.md — annotations must stay in sync with code.
										""")
						.version("0.4.0")
						.contact(new Contact().name("Template maintainers"))
						.license(new License().name("MIT")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
				.components(new Components()
						.addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
								.name(BEARER_SCHEME)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("JWT access token from POST /api/v1/auth/login")));
	}
}
