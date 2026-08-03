package com.template.api.platform;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enforces that Swagger / OpenAPI document contains expected API paths.
 * Extend this list whenever a public REST path is added or renamed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OpenApiContractTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiDocs_isAvailableOnLocal() throws Exception {
		mockMvc.perform(get("/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi").exists())
				.andExpect(jsonPath("$.info.title").value("Spring Boot API Template"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists());
	}

	@Test
	void apiDocs_containsAuthPaths() throws Exception {
		mockMvc.perform(get("/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/auth/login")))
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/auth/refresh")))
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/auth/logout")))
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/auth/register")));
	}

	@Test
	void apiDocs_containsUserPaths() throws Exception {
		mockMvc.perform(get("/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/users/me")))
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/users/me/password")))
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/users")));
	}

	@Test
	void apiDocs_containsItemPaths() throws Exception {
		mockMvc.perform(get("/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/items")))
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/items/{id}")));
	}

	@Test
	void apiDocs_containsNotePaths() throws Exception {
		mockMvc.perform(get("/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/notes")))
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/notes/{id}")));
	}

	@Test
	void apiDocs_documentsBearerScheme() throws Exception {
		mockMvc.perform(get("/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
	}
}
