package com.template.api.common.web;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.template.api.support.AuthTestSupport;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CorrelationIdFilterTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void health_generatesCorrelationIdWhenMissing() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(header().string(CorrelationIdFilter.HEADER, not(emptyOrNullString())));
	}

	@Test
	void health_echoesProvidedCorrelationId() throws Exception {
		mockMvc.perform(get("/actuator/health")
						.header(CorrelationIdFilter.HEADER, "fixed-corr-id-123"))
				.andExpect(status().isOk())
				.andExpect(header().string(CorrelationIdFilter.HEADER, "fixed-corr-id-123"));
	}

	@Test
	void health_blankHeader_generatesNewId() throws Exception {
		MvcResult result = mockMvc.perform(get("/actuator/health")
						.header(CorrelationIdFilter.HEADER, "   "))
				.andExpect(status().isOk())
				.andExpect(header().string(CorrelationIdFilter.HEADER, not(emptyOrNullString())))
				.andReturn();
		org.junit.jupiter.api.Assertions.assertNotEquals(
				"   ",
				result.getResponse().getHeader(CorrelationIdFilter.HEADER));
	}

	@Test
	void unauthorized_stillReturnsCorrelationId() throws Exception {
		mockMvc.perform(get("/api/v1/items"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(CorrelationIdFilter.HEADER, not(emptyOrNullString())));
	}

	@Test
	void authenticatedRequest_keepsClientCorrelationId() throws Exception {
		String auth = AuthTestSupport.adminBearer(mockMvc);
		mockMvc.perform(get("/api/v1/items")
						.header(HttpHeaders.AUTHORIZATION, auth)
						.header(CorrelationIdFilter.HEADER, "client-trace-99"))
				.andExpect(status().isOk())
				.andExpect(header().string(CorrelationIdFilter.HEADER, "client-trace-99"));
	}

	@Test
	void successiveRequests_withoutHeader_getDistinctIds() throws Exception {
		String first = mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getHeader(CorrelationIdFilter.HEADER);
		String second = mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getHeader(CorrelationIdFilter.HEADER);
		org.junit.jupiter.api.Assertions.assertNotEquals(first, second);
	}
}
