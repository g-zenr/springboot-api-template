package com.template.api.platform;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.template.api.support.AuthTestSupport;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ActuatorEndpointsTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void health_returnsUp() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void info_returnsOk() throws Exception {
		mockMvc.perform(get("/actuator/info"))
				.andExpect(status().isOk());
	}

	@Test
	void metrics_requiresAuthentication() throws Exception {
		mockMvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
	}

	@Test
	void metrics_withAuth_returnsOk() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(get("/actuator/metrics").header(HttpHeaders.AUTHORIZATION, auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.names").isArray());
	}

	@Test
	void prometheus_withAuth_returnsScrapeText() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(get("/actuator/prometheus")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.accept("text/plain", "application/openmetrics-text", "*/*"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("#")));
	}

	@Test
	void swaggerUi_isAvailableOnLocalProfile() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	void apiDocs_isAvailableOnLocalProfile() throws Exception {
		mockMvc.perform(get("/api-docs"))
				.andExpect(status().isOk());
	}
}
