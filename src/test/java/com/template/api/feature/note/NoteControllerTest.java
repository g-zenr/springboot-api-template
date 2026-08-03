package com.template.api.feature.note;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
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
class NoteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private String adminAuth;
	private String userAuth;

	@BeforeEach
	void setUp() throws Exception {
		adminAuth = AuthTestSupport.adminBearer(mockMvc);
		userAuth = AuthTestSupport.loginBearer(mockMvc, "user", "user123");
	}

	@Test
	void createNote_returns201() throws Exception {
		mockMvc.perform(post("/api/v1/notes")
						.header(HttpHeaders.AUTHORIZATION, userAuth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"Hello","body":"World"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Hello"))
				.andExpect(jsonPath("$.body").value("World"))
				.andExpect(jsonPath("$.ownerUsername").value("user"))
				.andExpect(jsonPath("$.id").isNotEmpty());
	}

	@Test
	void createNote_blankTitle_returns400() throws Exception {
		mockMvc.perform(post("/api/v1/notes")
						.header(HttpHeaders.AUTHORIZATION, userAuth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":" ","body":"x"}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createNote_withoutAuth_returns401() throws Exception {
		mockMvc.perform(post("/api/v1/notes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"NoAuth","body":"x"}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void listNotes_returnsOnlyMine() throws Exception {
		createNote(userAuth, "User Note", "u");
		createNote(adminAuth, "Admin Note", "a");

		mockMvc.perform(get("/api/v1/notes").header(HttpHeaders.AUTHORIZATION, userAuth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[?(@.title == 'User Note')]").exists())
				.andExpect(jsonPath("$.content[?(@.title == 'Admin Note')]").doesNotExist())
				.andExpect(jsonPath("$.page").value(0));
	}

	@Test
	void listNotes_searchAndLimit() throws Exception {
		createNote(userAuth, "Alpha Note Search", "body one");
		createNote(userAuth, "Beta Note Search", "body two");

		mockMvc.perform(get("/api/v1/notes")
						.param("search", "Alpha Note")
						.param("limit", "10")
						.header(HttpHeaders.AUTHORIZATION, userAuth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[?(@.title == 'Alpha Note Search')]").exists())
				.andExpect(jsonPath("$.content[?(@.title == 'Beta Note Search')]").doesNotExist());
	}

	@Test
	void listNotes_selectDeepOwner_populatesNestedUser() throws Exception {
		createNote(userAuth, "Deep Select Note", "body");

		mockMvc.perform(get("/api/v1/notes")
						.param("search", "Deep Select")
						.param("select", "id,title,owner.username,owner.roles")
						.header(HttpHeaders.AUTHORIZATION, userAuth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].title").value("Deep Select Note"))
				.andExpect(jsonPath("$.content[0].body").doesNotExist())
				.andExpect(jsonPath("$.content[0].owner.username").value("user"))
				.andExpect(jsonPath("$.content[0].owner.roles").isArray())
				.andExpect(jsonPath("$.content[0].owner.id").doesNotExist());
	}

	@Test
	void listNotes_includeCountOnly_countsOnlyMine() throws Exception {
		createNote(userAuth, "CountOnly Mine One", "a");
		createNote(userAuth, "CountOnly Mine Two", "b");
		createNote(adminAuth, "CountOnly Admin", "c");

		mockMvc.perform(get("/api/v1/notes")
						.param("include", "countOnly")
						.header(HttpHeaders.AUTHORIZATION, userAuth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(2))
				.andExpect(jsonPath("$.content").doesNotExist());
	}

	@Test
	void listNotes_includeCountOnly_withSearch() throws Exception {
		createNote(userAuth, "CountSearch Alpha", "a");
		createNote(userAuth, "CountSearch Beta", "b");

		mockMvc.perform(get("/api/v1/notes")
						.param("search", "Alpha")
						.param("include", "countOnly")
						.header(HttpHeaders.AUTHORIZATION, userAuth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1));
	}

	@Test
	void listNotes_includeCount_returnsContentAndTotals() throws Exception {
		createNote(userAuth, "IncludeCount Note A", "a");
		createNote(userAuth, "IncludeCount Note B", "b");

		mockMvc.perform(get("/api/v1/notes")
						.param("search", "IncludeCount Note")
						.param("include", "count")
						.param("limit", "10")
						.header(HttpHeaders.AUTHORIZATION, userAuth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.include").value("count"));
	}

	@Test
	void getNote_byId_returns200() throws Exception {
		String id = createNote(userAuth, "Get Me", "body");
		mockMvc.perform(get("/api/v1/notes/" + id).header(HttpHeaders.AUTHORIZATION, userAuth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Get Me"));
	}

	@Test
	void getNote_otherUsersNote_returns404() throws Exception {
		String id = createNote(adminAuth, "Secret", "admin only");
		mockMvc.perform(get("/api/v1/notes/" + id).header(HttpHeaders.AUTHORIZATION, userAuth))
				.andExpect(status().isNotFound());
	}

	@Test
	void getNote_unknown_returns404() throws Exception {
		mockMvc.perform(get("/api/v1/notes/00000000-0000-0000-0000-000000000099")
						.header(HttpHeaders.AUTHORIZATION, userAuth))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateNote_returns200() throws Exception {
		String id = createNote(userAuth, "Old", "b");
		mockMvc.perform(put("/api/v1/notes/" + id)
						.header(HttpHeaders.AUTHORIZATION, userAuth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"New","body":"updated"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("New"))
				.andExpect(jsonPath("$.body").value("updated"));
	}

	@Test
	void updateNote_otherUsers_returns404() throws Exception {
		String id = createNote(adminAuth, "Admin Owned", "x");
		mockMvc.perform(put("/api/v1/notes/" + id)
						.header(HttpHeaders.AUTHORIZATION, userAuth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"Hack","body":"nope"}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteNote_returns204() throws Exception {
		String id = createNote(userAuth, "Delete Me", "x");
		mockMvc.perform(delete("/api/v1/notes/" + id).header(HttpHeaders.AUTHORIZATION, userAuth))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/v1/notes/" + id).header(HttpHeaders.AUTHORIZATION, userAuth))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteNote_otherUsers_returns404() throws Exception {
		String id = createNote(adminAuth, "Admin Keep", "x");
		mockMvc.perform(delete("/api/v1/notes/" + id).header(HttpHeaders.AUTHORIZATION, userAuth))
				.andExpect(status().isNotFound());
	}

	@Test
	void listNotes_emptyForNewUserStyle() throws Exception {
		// admin list starts independent; just ensure list endpoint works
		mockMvc.perform(get("/api/v1/notes").header(HttpHeaders.AUTHORIZATION, adminAuth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.page").value(0));
	}

	@Test
	void createNote_nullBody_allowed() throws Exception {
		mockMvc.perform(post("/api/v1/notes")
						.header(HttpHeaders.AUTHORIZATION, userAuth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"Title Only"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Title Only"));
	}

	private String createNote(String auth, String title, String body) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/notes")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"%s","body":"%s"}
								""".formatted(title, body)))
				.andExpect(status().isCreated())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
	}
}
