package com.template.api.feature.item;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.template.api.common.error.ApiExceptionHandler;
import com.template.api.common.paging.PageResponse;
import com.template.api.config.security.JwtAuthenticationFilter;
import com.template.api.config.security.JwtService;
import com.template.api.config.security.authz.AuthorizationService;

/**
 * Slice test: web layer only — service and security beans mocked / simplified.
 */
@WebMvcTest(controllers = ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ItemControllerWebMvcTest.PermissiveSecurity.class, ApiExceptionHandler.class})
class ItemControllerWebMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ItemService itemService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@MockitoBean
	private AuthorizationService authorizationService;

	@MockitoBean
	private com.template.api.common.web.RateLimitService rateLimitService;

	@MockitoBean
	private com.template.api.common.web.RateLimitProperties rateLimitProperties;

	@Test
	void list_returnsPageEnvelopeFromService() throws Exception {
		ItemResponse row = new ItemResponse(
				UUID.randomUUID(), "WebMvc Item", "d", Instant.now(), Instant.now());
		when(itemService.findAll(any())).thenReturn(new PageResponse<>(
				List.of(row), 0, 20, 1, 1, true, true, false, false, null, null, null, null));

		mockMvc.perform(get("/api/v1/items"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].name").value("WebMvc Item"))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void create_returns201AndLocation() throws Exception {
		UUID id = UUID.randomUUID();
		when(itemService.create(any())).thenReturn(
				new ItemResponse(id, "Created Via Slice", null, Instant.now(), Instant.now()));

		mockMvc.perform(post("/api/v1/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Created Via Slice"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/v1/items/" + id))
				.andExpect(jsonPath("$.name").value("Created Via Slice"));
	}

	@Test
	void create_blankName_returns400() throws Exception {
		mockMvc.perform(post("/api/v1/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":" "}
								"""))
				.andExpect(status().isBadRequest());
	}

	@TestConfiguration
	static class PermissiveSecurity {
		@Bean
		SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
			http.csrf(AbstractHttpConfigurer::disable)
					.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
			return http.build();
		}
	}
}
