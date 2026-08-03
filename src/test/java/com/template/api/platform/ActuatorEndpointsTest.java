package com.template.api.platform;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
	void actuatorRoot_listsLinks() throws Exception {
		mockMvc.perform(get("/actuator"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$._links.health").exists())
				.andExpect(jsonPath("$._links.info").exists());
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
