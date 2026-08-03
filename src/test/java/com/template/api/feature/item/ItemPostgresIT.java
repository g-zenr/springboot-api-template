package com.template.api.feature.item;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Postgres integration via Testcontainers. Skipped automatically when Docker is unavailable.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class ItemPostgresIT {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("template")
			.withUsername("template")
			.withPassword("template");

	@DynamicPropertySource
	static void datasourceProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
		registry.add("spring.h2.console.enabled", () -> "false");
	}

	@Autowired
	private ItemRepository itemRepository;

	@Test
	void flywayAndRepository_workAgainstPostgres() {
		Item saved = itemRepository.save(new Item("Postgres Item", "from testcontainers"));
		assertThat(saved.getId()).isNotNull();
		assertThat(itemRepository.findById(saved.getId())).isPresent();
		assertThat(itemRepository.existsByNameIgnoreCase("postgres item")).isTrue();
	}

	@Test
	void duplicateName_isRejectedByUniqueIndex() {
		itemRepository.save(new Item("Unique PG Name", "a"));
		Item second = new Item("Unique PG Name", "b");
		org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
			itemRepository.saveAndFlush(second);
		});
	}
}
