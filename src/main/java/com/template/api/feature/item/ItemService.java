package com.template.api.feature.item;

import java.util.Set;
import java.util.UUID;

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

	static final Set<String> SORT_FIELDS = Set.of("name", "description", "createdAt", "updatedAt");
	static final Set<String> SELECT_ROOTS = Set.of("id", "name", "description", "createdAt", "updatedAt");
	static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "name");

	private final ItemRepository itemRepository;
	private final ItemMapper itemMapper;
	private final PagedReads pagedReads;

	public ItemService(ItemRepository itemRepository, ItemMapper itemMapper, PagedReads pagedReads) {
		this.itemRepository = itemRepository;
		this.itemMapper = itemMapper;
		this.pagedReads = pagedReads;
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
		ItemResponse full = itemMapper.toResponse(getItem(id));
		return FieldSelection.applyOne(full, select, SELECT_ROOTS);
	}

	@Transactional(readOnly = true)
	public ItemResponse findById(UUID id) {
		return itemMapper.toResponse(getItem(id));
	}

	public ItemResponse create(ItemRequest request) {
		if (itemRepository.existsByNameIgnoreCase(request.name())) {
			throw new ConflictException("Item name already exists: " + request.name());
		}
		Item item = itemMapper.toNewEntity(request);
		return itemMapper.toResponse(itemRepository.save(item));
	}

	public ItemResponse update(UUID id, ItemRequest request) {
		Item item = getItem(id);
		if (itemRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
			throw new ConflictException("Item name already exists: " + request.name());
		}
		item.setName(request.name().trim());
		item.setDescription(itemMapper.trimToNull(request.description()));
		return itemMapper.toResponse(itemRepository.save(item));
	}

	public void delete(UUID id) {
		if (!itemRepository.existsById(id)) {
			throw new ResourceNotFoundException("Item not found: " + id);
		}
		itemRepository.deleteById(id);
	}

	private Item getItem(UUID id) {
		return itemRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
	}
}
