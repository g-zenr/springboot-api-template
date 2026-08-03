package com.template.api.feature.item;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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

/**
 * Sample feature suite — demonstrates testing-policy minimums (≥ 10 cases per feature).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ItemControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private String auth;

	@BeforeEach
	void authenticate() throws Exception {
		auth = AuthTestSupport.adminBearer(mockMvc);
	}

	@Test
	void createItem_returns201WithBodyAndLocation() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson("Alpha Item", "First description")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/v1/items/")))
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.name").value("Alpha Item"))
				.andExpect(jsonPath("$.description").value("First description"))
				.andExpect(jsonPath("$.createdAt").isNotEmpty())
				.andExpect(jsonPath("$.updatedAt").isNotEmpty())
				.andReturn();

		String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
		UUID.fromString(id);
	}

	@Test
	void createItem_withNullDescription_succeeds() throws Exception {
		mockMvc.perform(post("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "name": "No Description Item" }
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("No Description Item"));
	}

	@Test
	void createItem_blankName_returns400ValidationFailed() throws Exception {
		mockMvc.perform(post("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson(" ", "invalid")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation Failed"))
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void createItem_nameTooLong_returns400() throws Exception {
		String longName = "n".repeat(121);
		mockMvc.perform(post("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson(longName, "too long")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation Failed"));
	}

	@Test
	void createItem_duplicateName_returns409Conflict() throws Exception {
		mockMvc.perform(post("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson("Dup Name", "first")))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson("Dup Name", "second")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Conflict"))
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void createItem_duplicateNameCaseInsensitive_returns409() throws Exception {
		mockMvc.perform(post("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson("Case Name", "first")))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson("case name", "second")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Conflict"));
	}

	@Test
	void listItems_returns200AndIncludesCreatedItem() throws Exception {
		createItem("Listable Item", "listed");

		mockMvc.perform(get("/api/v1/items").header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$.content[?(@.name == 'Listable Item')]").exists())
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.limit").value(20))
				.andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)));
	}

	@Test
	void listItems_search_filtersByNameOrDescription() throws Exception {
		createItem("Search Alpha Unique", "zzz");
		createItem("Other Item Unique", "search beta phrase");

		mockMvc.perform(get("/api/v1/items")
						.param("search", "Alpha Unique")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[?(@.name == 'Search Alpha Unique')]").exists())
				.andExpect(jsonPath("$.content[?(@.name == 'Other Item Unique')]").doesNotExist())
				.andExpect(jsonPath("$.search").value("Alpha Unique"));

		mockMvc.perform(get("/api/v1/items")
						.param("search", "beta phrase")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[?(@.name == 'Other Item Unique')]").exists());
	}

	@Test
	void listItems_pagination_limitAndPage() throws Exception {
		createItem("Page Item A", "p");
		createItem("Page Item B", "p");
		createItem("Page Item C", "p");

		mockMvc.perform(get("/api/v1/items")
						.param("search", "Page Item")
						.param("limit", "2")
						.param("page", "0")
						.param("sort", "name,asc")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.limit").value(2))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.totalPages").value(2))
				.andExpect(jsonPath("$.hasNext").value(true));

		mockMvc.perform(get("/api/v1/items")
						.param("search", "Page Item")
						.param("limit", "2")
						.param("page", "1")
						.param("sort", "name,asc")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.page").value(1))
				.andExpect(jsonPath("$.hasNext").value(false));
	}

	@Test
	void listItems_sortByNameDesc() throws Exception {
		createItem("Sort Zebra Unique", "z");
		createItem("Sort Apple Unique", "a");

		mockMvc.perform(get("/api/v1/items")
						.param("search", "Sort")
						.param("sort", "name,desc")
						.param("limit", "10")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].name").value("Sort Zebra Unique"));
	}

	@Test
	void listItems_invalidLimit_returns400() throws Exception {
		mockMvc.perform(get("/api/v1/items")
						.param("limit", "0")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Bad Request"));
	}

	@Test
	void listItems_invalidSortField_returns400() throws Exception {
		mockMvc.perform(get("/api/v1/items")
						.param("sort", "notAField")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Bad Request"));
	}

	@Test
	void listItems_select_returnsOnlyChosenFields() throws Exception {
		createItem("Select Sparse Item", "hidden-desc");

		mockMvc.perform(get("/api/v1/items")
						.param("search", "Select Sparse")
						.param("select", "id,name")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.select").value("id,name"))
				.andExpect(jsonPath("$.content[0].name").value("Select Sparse Item"))
				.andExpect(jsonPath("$.content[0].id").isNotEmpty())
				.andExpect(jsonPath("$.content[0].description").doesNotExist())
				.andExpect(jsonPath("$.content[0].createdAt").doesNotExist());
	}

	@Test
	void listItems_invalidSelect_returns400() throws Exception {
		mockMvc.perform(get("/api/v1/items")
						.param("select", "notAField")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Bad Request"));
	}

	@Test
	void getItem_select_sparseFields() throws Exception {
		String id = createItem("Get Select Item", "details");

		mockMvc.perform(get("/api/v1/items/" + id)
						.param("select", "name")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Get Select Item"))
				.andExpect(jsonPath("$.id").doesNotExist())
				.andExpect(jsonPath("$.description").doesNotExist());
	}

	@Test
	void listItems_defaultInclude_computesTotalCount() throws Exception {
		createItem("Count Default A", "c");
		createItem("Count Default B", "c");

		mockMvc.perform(get("/api/v1/items")
						.param("search", "Count Default")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.include").doesNotExist());
	}

	@Test
	void listItems_includeCount_computesTotalCount() throws Exception {
		createItem("Include Count A", "c");
		createItem("Include Count B", "c");
		createItem("Include Count C", "c");

		mockMvc.perform(get("/api/v1/items")
						.param("search", "Include Count")
						.param("limit", "2")
						.param("include", "count")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.include").value("count"))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.totalPages").value(2))
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.hasNext").value(true));
	}

	@Test
	void listItems_includeWithoutCount_skipsTotalCount() throws Exception {
		createItem("Skip Count A", "c");
		createItem("Skip Count B", "c");

		mockMvc.perform(get("/api/v1/items")
						.param("search", "Skip Count")
						.param("include", "summary")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.include").value("summary"))
				.andExpect(jsonPath("$.totalElements").value(-1))
				.andExpect(jsonPath("$.totalPages").value(-1))
				.andExpect(jsonPath("$.content", hasSize(2)));
	}

	@Test
	void listItems_eachReservedIncludeAlone_stillReturnsContent() throws Exception {
		createItem("Reserved Include Item", "r");
		// Reserved tokens are accepted; content still returned (no countOnly)
		for (String token : new String[] {
				"pagination", "summary", "statistics", "relations", "permissions", "analytics"
		}) {
			mockMvc.perform(get("/api/v1/items")
							.param("search", "Reserved Include")
							.param("include", token)
							.header(HttpHeaders.AUTHORIZATION, auth))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content", hasSize(1)))
					.andExpect(jsonPath("$.totalElements").value(-1))
					.andExpect(jsonPath("$.include").value(token));
		}
	}

	@Test
	void listItems_includeUnknown_ignoredAndKeepsDefaultCount() throws Exception {
		createItem("Unknown Include Item", "c");

		mockMvc.perform(get("/api/v1/items")
						.param("search", "Unknown Include")
						.param("include", "not-a-real-option")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void listItems_includeCountOnly_returnsOnlyCount() throws Exception {
		createItem("CountOnly A", "c");
		createItem("CountOnly B", "c");
		createItem("CountOnly C", "c");

		mockMvc.perform(get("/api/v1/items")
						.param("search", "CountOnly")
						.param("include", "countOnly")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(3))
				.andExpect(jsonPath("$.content").doesNotExist())
				.andExpect(jsonPath("$.totalElements").doesNotExist());
	}

	@Test
	void listItems_includeCountOnly_withSearch_filtersCount() throws Exception {
		createItem("CountOnlyFilter Match", "x");
		createItem("CountOnlyFilter Other", "y");

		mockMvc.perform(get("/api/v1/items")
						.param("search", "Match")
						.param("include", "countOnly")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1));
	}

	@Test
	void listItems_includeCountOnly_caseInsensitive() throws Exception {
		createItem("CountOnlyCase Item", "c");

		mockMvc.perform(get("/api/v1/items")
						.param("search", "CountOnlyCase")
						.param("include", "COUNTONLY")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1))
				.andExpect(jsonPath("$.content").doesNotExist());
	}

	@Test
	void getItem_byId_returns200() throws Exception {
		String id = createItem("Gettable Item", "details");

		mockMvc.perform(get("/api/v1/items/" + id).header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.name").value("Gettable Item"))
				.andExpect(jsonPath("$.description").value("details"));
	}

	@Test
	void getItem_unknownId_returns404() throws Exception {
		UUID missing = UUID.randomUUID();

		mockMvc.perform(get("/api/v1/items/" + missing).header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Resource Not Found"))
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void updateItem_returns200WithNewFields() throws Exception {
		String id = createItem("Before Update", "old");

		mockMvc.perform(put("/api/v1/items/" + id)
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson("After Update", "new")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.name").value("After Update"))
				.andExpect(jsonPath("$.description").value("new"));
	}

	@Test
	void updateItem_unknownId_returns404() throws Exception {
		UUID missing = UUID.randomUUID();

		mockMvc.perform(put("/api/v1/items/" + missing)
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson("Ghost", "nope")))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Resource Not Found"));
	}

	@Test
	void updateItem_duplicateName_returns409() throws Exception {
		createItem("Taken Name", "a");
		String id = createItem("Other Name", "b");

		mockMvc.perform(put("/api/v1/items/" + id)
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson("Taken Name", "clash")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Conflict"));
	}

	@Test
	void updateItem_blankName_returns400() throws Exception {
		String id = createItem("Valid Before Blank", "x");

		mockMvc.perform(put("/api/v1/items/" + id)
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson(" ", "bad")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation Failed"));
	}

	@Test
	void deleteItem_returns204AndSubsequentGetIs404() throws Exception {
		String id = createItem("Deletable Item", "bye");

		mockMvc.perform(delete("/api/v1/items/" + id).header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/items/" + id).header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteItem_unknownId_returns404() throws Exception {
		UUID missing = UUID.randomUUID();

		mockMvc.perform(delete("/api/v1/items/" + missing).header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Resource Not Found"));
	}

	@Test
	void softDelete_allowsReusingSameName() throws Exception {
		String id = createItem("Reusable Soft Name", "first");

		mockMvc.perform(delete("/api/v1/items/" + id).header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson("Reusable Soft Name", "second")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Reusable Soft Name"));
	}

	@Test
	void softDelete_excludesItemFromList() throws Exception {
		String name = "List Soft Gone " + UUID.randomUUID();
		String id = createItem(name, "x");

		mockMvc.perform(delete("/api/v1/items/" + id).header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/items")
						.param("search", name)
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(0)));
	}

	@Test
	void softDelete_secondDelete_returns404() throws Exception {
		String id = createItem("Double Soft Delete", "x");

		mockMvc.perform(delete("/api/v1/items/" + id).header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/v1/items/" + id).header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isNotFound());
	}

	@Test
	void getItem_afterUpdate_returnsFreshDescription_notStaleCache() throws Exception {
		String id = createItem("Cache Evict Item", "before");

		mockMvc.perform(get("/api/v1/items/" + id).header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.description").value("before"));

		mockMvc.perform(put("/api/v1/items/" + id)
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson("Cache Evict Item", "after")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.description").value("after"));

		mockMvc.perform(get("/api/v1/items/" + id).header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.description").value("after"));
	}

	@Test
	void getItem_withSelect_stillWorksWithCache() throws Exception {
		String id = createItem("Select Cache Item", "full");

		mockMvc.perform(get("/api/v1/items/" + id)
						.param("select", "id,name")
						.header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.name").value("Select Cache Item"))
				.andExpect(jsonPath("$.description").doesNotExist());
	}

	private String createItem(String name, String description) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(itemJson(name, description)))
				.andExpect(status().isCreated())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
	}

	private static String itemJson(String name, String description) {
		return """
				{
				  "name": "%s",
				  "description": "%s"
				}
				""".formatted(name, description);
	}
}
