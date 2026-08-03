package com.template.api.feature.item;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/**
 * Slice test: JPA + repository only (faster than full {@code @SpringBootTest}).
 */
@DataJpaTest
class ItemRepositoryDataJpaTest {

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void saveAndFindByNameIgnoreCase() {
		Item saved = itemRepository.save(new Item("Slice Item", "from DataJpaTest"));
		assertThat(saved.getId()).isNotNull();
		assertThat(itemRepository.findByNameIgnoreCase("slice item")).isPresent();
		assertThat(itemRepository.existsByNameIgnoreCase("SLICE ITEM")).isTrue();
	}

	@Test
	void existsByNameIgnoreCaseAndIdNot_detectsConflict() {
		Item first = itemRepository.save(new Item("Unique Slice", "a"));
		Item second = itemRepository.save(new Item("Other Slice", "b"));
		assertThat(itemRepository.existsByNameIgnoreCaseAndIdNot("unique slice", second.getId())).isTrue();
		assertThat(itemRepository.existsByNameIgnoreCaseAndIdNot("unique slice", first.getId())).isFalse();
	}

	@Test
	void softDelete_hidesRowFromFindById() {
		Item saved = itemRepository.saveAndFlush(new Item("Soft Slice", "gone"));
		saved.softDelete();
		itemRepository.saveAndFlush(saved);
		entityManager.clear();
		assertThat(itemRepository.findById(saved.getId())).isEmpty();
	}
}
