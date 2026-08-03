package com.template.api.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

public final class AuthTestSupport {

	private AuthTestSupport() {
	}

	public static String loginBearer(MockMvc mockMvc, String username, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"%s","password":"%s"}
								""".formatted(username, password)))
				.andExpect(status().isOk())
				.andReturn();
		String token = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
		return "Bearer " + token;
	}

	public static String adminBearer(MockMvc mockMvc) throws Exception {
		return loginBearer(mockMvc, "admin", "admin123");
	}
}
