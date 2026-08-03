package com.template.api.feature.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import com.template.api.common.error.ResourceNotFoundException;

/**
 * Soft-delete + item cache behavior (service layer).
 */
@SpringBootTest
@Transactional
class ItemSoftDeleteAndCacheTest {

	@Autowired
	private ItemService itemService;

	@Autowired
	private CacheManager cacheManager;

	@Test
	void softDelete_getById_throwsNotFound() {
		ItemResponse created = itemService.create(new ItemRequest("Svc Soft Del", "d"));
		itemService.delete(created.id());
		assertThatThrownBy(() -> itemService.findById(created.id()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void softDelete_nameCanBeReused() {
		ItemResponse first = itemService.create(new ItemRequest("Svc Reuse Name", "a"));
		itemService.delete(first.id());
		ItemResponse second = itemService.create(new ItemRequest("Svc Reuse Name", "b"));
		assertThat(second.id()).isNotEqualTo(first.id());
		assertThat(second.name()).isEqualTo("Svc Reuse Name");
	}

	@Test
	void findById_populatesCache() {
		ItemResponse created = itemService.create(new ItemRequest("Cached Item", "c"));
		itemService.findById(created.id());
		Cache cache = cacheManager.getCache(ItemService.CACHE_NAME);
		assertThat(cache).isNotNull();
		assertThat(cache.get(created.id())).isNotNull();
	}

	@Test
	void update_evictsCacheEntry() {
		ItemResponse created = itemService.create(new ItemRequest("Evict Me", "old"));
		itemService.findById(created.id());
		Cache cache = cacheManager.getCache(ItemService.CACHE_NAME);
		assertThat(cache).isNotNull();
		assertThat(cache.get(created.id())).isNotNull();

		itemService.update(created.id(), new ItemRequest("Evict Me", "new"));
		assertThat(cache.get(created.id())).isNull();

		ItemResponse reloaded = itemService.findById(created.id());
		assertThat(reloaded.description()).isEqualTo("new");
	}

	@Test
	void delete_evictsCacheAndMissesAfterwards() {
		ItemResponse created = itemService.create(new ItemRequest("Del Cache", "x"));
		itemService.findById(created.id());
		Cache cache = cacheManager.getCache(ItemService.CACHE_NAME);
		assertThat(cache).isNotNull();
		assertThat(cache.get(created.id())).isNotNull();

		itemService.delete(created.id());
		assertThat(cache.get(created.id())).isNull();
		assertThatThrownBy(() -> itemService.findById(created.id()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void softDelete_unknownId_throws() {
		assertThatThrownBy(() -> itemService.delete(UUID.randomUUID()))
				.isInstanceOf(ResourceNotFoundException.class);
	}
}
