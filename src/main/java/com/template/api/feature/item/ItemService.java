package com.template.api.feature.item;

import java.util.Set;
import java.util.UUID;
import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.template.api.common.error.ConflictException;
import com.template.api.common.error.ResourceNotFoundException;
import com.template.api.common.paging.PageQuery;
import com.template.api.common.paging.PageResponse;
import com.template.api.common.paging.Pageables;
import com.template.api.common.paging.PagedReads;
import com.template.api.common.paging.Specs;
import com.template.api.common.select.FieldSelection;

@Service
@Transactional
public class ItemService {

	/** Cache name for single-item reads (see CacheConfig). */
	public static final String CACHE_NAME = "items";

	static final Set<String> SORT_FIELDS = Set.of("name", "description", "createdAt", "updatedAt");
	static final Set<String> SELECT_ROOTS = Set.of("id", "name", "description", "createdAt", "updatedAt");
	static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "name");

	private final ItemRepository itemRepository;
	private final ItemMapper itemMapper;
	private final PagedReads pagedReads;
	private final EntityManager entityManager;
	/** Proxy for @Cacheable self-invocation (select path → full findById). */
	private final ObjectProvider<ItemService> itemService;

	public ItemService(
			ItemRepository itemRepository,
			ItemMapper itemMapper,
			PagedReads pagedReads,
			EntityManager entityManager,
			ObjectProvider<ItemService> itemService) {
		this.itemRepository = itemRepository;
		this.itemMapper = itemMapper;
		this.pagedReads = pagedReads;
		this.entityManager = entityManager;
		this.itemService = itemService;
	}

	@Transactional(readOnly = true)
	public Object findAll(PageQuery query) {
		Specification<Item> spec = query.normalizedSearch()
				.map(ItemSpecs::matchesSearch)
				.orElseGet(Specs::unrestricted);
		if (query.includes().wantsCountOnly()) {
			return pagedReads.countOnly(Item.class, spec);
		}
		Pageable pageable = Pageables.toPageable(query, SORT_FIELDS, DEFAULT_SORT);
		PageResponse<ItemResponse> page =
				pagedReads.find(Item.class, spec, pageable, query, itemMapper::toResponse);
		return FieldSelection.apply(page, query, SELECT_ROOTS);
	}

	@Transactional(readOnly = true)
	public Object findById(UUID id, String select) {
		ItemResponse full = itemService.getObject().findById(id);
		return FieldSelection.applyOne(full, select, SELECT_ROOTS);
	}

	@Cacheable(cacheNames = CACHE_NAME, key = "#id")
	@Transactional(readOnly = true)
	public ItemResponse findById(UUID id) {
		return itemMapper.toResponse(getItem(id));
	}

	@CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
	public ItemResponse create(ItemRequest request) {
		if (itemRepository.existsByNameIgnoreCase(request.name())) {
			throw new ConflictException("Item name already exists: " + request.name());
		}
		Item item = itemMapper.toNewEntity(request);
		return itemMapper.toResponse(itemRepository.save(item));
	}

	@CacheEvict(cacheNames = CACHE_NAME, key = "#id")
	public ItemResponse update(UUID id, ItemRequest request) {
		Item item = getItem(id);
		if (itemRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
			throw new ConflictException("Item name already exists: " + request.name());
		}
		item.setName(request.name().trim());
		item.setDescription(itemMapper.trimToNull(request.description()));
		return itemMapper.toResponse(itemRepository.save(item));
	}

	/**
	 * Soft-delete: sets {@code deleted_at}. Row is hidden by {@code @SQLRestriction};
	 * the same name can be reused afterward (partial unique index).
	 */
	@CacheEvict(cacheNames = CACHE_NAME, key = "#id")
	public void delete(UUID id) {
		Item item = getItem(id);
		item.softDelete();
		itemRepository.saveAndFlush(item);
		// Detach so later findById re-queries (SQLRestriction) instead of L1 cache
		entityManager.detach(item);
	}

	private Item getItem(UUID id) {
		return itemRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
	}
}
