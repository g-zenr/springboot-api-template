package com.template.api.feature.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ItemMapperTest {

	private ItemMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(ItemMapper.class);
	}

	@Test
	void toResponse_mapsAllFields() {
		Item item = new Item("Name", "Desc");
		item.setId(UUID.randomUUID());
		// timestamps set via reflection-like path: use prePersist simulation
		ItemResponse response = mapper.toResponse(item);
		assertEquals(item.getId(), response.id());
		assertEquals("Name", response.name());
		assertEquals("Desc", response.description());
	}

	@Test
	void toNewEntity_trimsName() {
		Item entity = mapper.toNewEntity(new ItemRequest("  Hello  ", "  world  "));
		assertEquals("Hello", entity.getName());
		assertEquals("world", entity.getDescription());
		assertNull(entity.getDeletedAt());
	}

	@Test
	void softDelete_setsDeletedAt() {
		Item item = new Item("Soft", "x");
		assertNull(item.getDeletedAt());
		item.softDelete();
		assertNotNull(item.getDeletedAt());
		org.junit.jupiter.api.Assertions.assertTrue(item.isDeleted());
	}

	@Test
	void toNewEntity_blankDescriptionBecomesNull() {
		Item entity = mapper.toNewEntity(new ItemRequest("OnlyName", "   "));
		assertEquals("OnlyName", entity.getName());
		assertNull(entity.getDescription());
	}

	@Test
	void toNewEntity_nullDescriptionStaysNull() {
		Item entity = mapper.toNewEntity(new ItemRequest("OnlyName", null));
		assertNull(entity.getDescription());
	}

	@Test
	void trimToNull_helpers() {
		assertNull(mapper.trimToNull(null));
		assertNull(mapper.trimToNull("  "));
		assertEquals("x", mapper.trimToNull(" x "));
		assertEquals("a", mapper.trim(" a "));
	}

	@Test
	void toResponse_preservesTimestamps() throws Exception {
		Item item = new Item("T", "D");
		var createdField = Item.class.getDeclaredField("createdAt");
		var updatedField = Item.class.getDeclaredField("updatedAt");
		createdField.setAccessible(true);
		updatedField.setAccessible(true);
		Instant now = Instant.parse("2026-01-01T00:00:00Z");
		createdField.set(item, now);
		updatedField.set(item, now);

		ItemResponse response = mapper.toResponse(item);
		assertEquals(now, response.createdAt());
		assertEquals(now, response.updatedAt());
		assertNotNull(response);
	}
}
