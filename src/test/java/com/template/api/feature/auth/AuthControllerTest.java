package com.template.api.feature.auth;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.template.api.support.AuthTestSupport;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void login_admin_returnsAccessAndRefreshTokens() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"admin","password":"admin123"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresInMs").isNumber())
				.andExpect(jsonPath("$.refreshExpiresInMs").isNumber())
				.andExpect(jsonPath("$.username").value("admin"))
				.andExpect(jsonPath("$.roles", hasItem("ADMIN")));
	}

	@Test
	void login_user_returnsUserRole() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"user","password":"user123"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("user"))
				.andExpect(jsonPath("$.roles", hasItem("USER")));
	}

	@Test
	void login_wrongPassword_returns401() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"admin","password":"wrong"}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void login_unknownUser_returns401() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"ghost","password":"admin123"}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void login_blankUsername_returns400() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":" ","password":"admin123"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation Failed"));
	}

	@Test
	void login_blankPassword_returns400() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"admin","password":" "}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation Failed"));
	}

	@Test
	void login_missingBodyFields_returns400() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void protectedEndpoint_withoutToken_returns401() throws Exception {
		mockMvc.perform(get("/api/v1/items"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpoint_withValidToken_returns200() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(get("/api/v1/items").header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk());
	}

	@Test
	void protectedEndpoint_withInvalidToken_returns401() throws Exception {
		mockMvc.perform(get("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-jwt"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpoint_withMalformedAuthHeader_returns401() throws Exception {
		mockMvc.perform(get("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, "Token abc"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void refresh_returnsNewTokenPair() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"admin","password":"admin123"}
								"""))
				.andExpect(status().isOk())
				.andReturn();
		String refresh = JsonPath.read(login.getResponse().getContentAsString(), "$.refreshToken");
		String oldAccess = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"refreshToken":"%s"}
								""".formatted(refresh)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").isNotEmpty())
				.andExpect(jsonPath("$.accessToken", not(oldAccess)));
	}

	@Test
	void refresh_withInvalidToken_returns401() throws Exception {
		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"refreshToken":"does-not-exist"}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void logout_revokesRefreshToken() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"user","password":"user123"}
								"""))
				.andExpect(status().isOk())
				.andReturn();
		String refresh = JsonPath.read(login.getResponse().getContentAsString(), "$.refreshToken");

		mockMvc.perform(post("/api/v1/auth/logout")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"refreshToken":"%s"}
								""".formatted(refresh)))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"refreshToken":"%s"}
								""".formatted(refresh)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void token_canBeUsedToCreateItem() throws Exception {
		String auth = AuthTestSupport.loginBearer(mockMvc, "user", "user123");
		mockMvc.perform(post("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Auth Created Item","description":"via user token"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Auth Created Item"));
	}

	@Test
	void refresh_rotatedToken_cannotBeReused() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"admin","password":"admin123"}
								"""))
				.andExpect(status().isOk())
				.andReturn();
		String refresh = JsonPath.read(login.getResponse().getContentAsString(), "$.refreshToken");

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"refreshToken":"%s"}
								""".formatted(refresh)))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"refreshToken":"%s"}
								""".formatted(refresh)))
				.andExpect(status().isUnauthorized());
	}
}
