package com.template.api.feature.user;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.template.api.support.AuthTestSupport;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void me_returnsCurrentUser() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("admin"))
				.andExpect(jsonPath("$.roles", hasItem("ADMIN")));
	}

	@Test
	void me_withoutAuth_returns401() throws Exception {
		mockMvc.perform(get("/api/v1/users/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void listUsers_asAdmin_returnsOk() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[?(@.username == 'admin')]").exists())
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.limit").value(20));
	}

	@Test
	void listUsers_searchByUsername() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(get("/api/v1/users")
						.param("search", "adm")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[?(@.username == 'admin')]").exists())
				.andExpect(jsonPath("$.search").value("adm"));
	}

	@Test
	void listUsers_includeCountOnly_returnsOnlyCount() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		// demo seed: admin + user at minimum
		mockMvc.perform(get("/api/v1/users")
						.param("include", "countOnly")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
				.andExpect(jsonPath("$.content").doesNotExist())
				.andExpect(jsonPath("$.totalElements").doesNotExist());
	}

	@Test
	void listUsers_includeCountOnly_withSearch() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(get("/api/v1/users")
						.param("search", "admin")
						.param("include", "countOnly")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1));
	}

	@Test
	void listUsers_select_returnsSparseFields() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(get("/api/v1/users")
						.param("search", "admin")
						.param("select", "id,username")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].username").value("admin"))
				.andExpect(jsonPath("$.content[0].id").isNotEmpty())
				.andExpect(jsonPath("$.content[0].roles").doesNotExist())
				.andExpect(jsonPath("$.select").value("id,username"));
	}

	@Test
	void listUsers_includeCount_returnsContentAndTotals() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(get("/api/v1/users")
						.param("include", "count")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
				.andExpect(jsonPath("$.include").value("count"));
	}

	@Test
	void me_select_returnsSparseFields() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(get("/api/v1/users/me")
						.param("select", "username,roles")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("admin"))
				.andExpect(jsonPath("$.roles").isArray())
				.andExpect(jsonPath("$.id").doesNotExist());
	}

	@Test
	void listUsers_asUser_returns403() throws Exception {
		String auth = AuthTestSupport.loginBearer(mockMvc, "user", "user123");
		mockMvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminCreateUser_thenLoginWorks() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(post("/api/v1/users")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"newbie","password":"password1","roles":["USER"]}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("newbie"))
				.andExpect(jsonPath("$.roles", hasItem("USER")));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"newbie","password":"password1"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());
	}

	@Test
	void adminCreateUser_duplicate_returns409() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(post("/api/v1/users")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"admin","password":"password1","roles":["USER"]}
								"""))
				.andExpect(status().isConflict());
	}

	@Test
	void register_createsUserAndAllowsLogin() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"selfreg","password":"password1"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("selfreg"))
				.andExpect(jsonPath("$.roles", hasItem("USER")));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"selfreg","password":"password1"}
								"""))
				.andExpect(status().isOk());
	}

	@Test
	void register_shortPassword_returns400() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"shortpw","password":"abc"}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void changePassword_thenOldPasswordFails() throws Exception {
		String auth = AuthTestSupport.loginBearer(mockMvc, "user", "user123");
		mockMvc.perform(put("/api/v1/users/me/password")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"currentPassword":"user123","newPassword":"newpass99"}
								"""))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"user","password":"user123"}
								"""))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"user","password":"newpass99"}
								"""))
				.andExpect(status().isOk());
	}

	@Test
	void changePassword_wrongCurrent_returns401() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(put("/api/v1/users/me/password")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"currentPassword":"wrong","newPassword":"newpass99"}
								"""))
				.andExpect(status().isUnauthorized());
	}
}
