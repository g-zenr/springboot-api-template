package com.template.api.feature.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ItemMapperSpringTest {

	@Autowired
	private ItemMapper itemMapper;

	@Autowired
	private ItemService itemService;

	@Test
	void springInjectsMapStructMapper() {
		assertNotNull(itemMapper);
		Item entity = itemMapper.toNewEntity(new ItemRequest("Mapped", "via mapstruct"));
		assertEquals("Mapped", entity.getName());
	}

	@Test
	void serviceCreate_usesMapper() {
		ItemResponse created = itemService.create(new ItemRequest("Service Mapped", "ok"));
		assertNotNull(created.id());
		assertEquals("Service Mapped", created.name());
	}
}
