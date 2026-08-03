package com.template.api.config.security.authz;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.template.api.support.AuthTestSupport;

/**
 * Runs the app with {@code app.security.authorization.mode=abac}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "app.security.authorization.mode=abac")
class AbacModeIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void abacMode_adminCanListUsers() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());
	}

	@Test
	void abacMode_userCannotListUsers() throws Exception {
		String auth = AuthTestSupport.loginBearer(mockMvc, "user", "user123");
		mockMvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isForbidden());
	}

	@Test
	void abacMode_adminCanReadOtherUsersNote() throws Exception {
		String userAuth = AuthTestSupport.loginBearer(mockMvc, "user", "user123");
		String adminAuth = AuthTestSupport.adminBearer(mockMvc);

		MvcResult created = mockMvc.perform(post("/api/v1/notes")
						.header(HttpHeaders.AUTHORIZATION, userAuth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"ABAC Admin Read","body":"secret"}
								"""))
				.andExpect(status().isCreated())
				.andReturn();
		String id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

		mockMvc.perform(get("/api/v1/notes/" + id).header(HttpHeaders.AUTHORIZATION, adminAuth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("ABAC Admin Read"));
	}
}
