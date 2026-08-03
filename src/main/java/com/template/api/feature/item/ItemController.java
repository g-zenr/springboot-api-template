package com.template.api.feature.item;

import java.net.URI;
import java.util.UUID;
import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.template.api.common.paging.PageQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/items")
@Tag(name = "Items", description = "Shared sample catalog CRUD (Bearer JWT)")
public class ItemController {

	private final ItemService itemService;

	public ItemController(ItemService itemService) {
		this.itemService = itemService;
	}

	@GetMapping
	@Operation(
			summary = "List items (paginated)",
			description = "Reusable query: page (0-based), limit (1–100), sort (e.g. name,asc), "
					+ "search (name/description), select (sparse fields, e.g. id,name), "
					+ "include values: count (docs+totals), countOnly ({count} only), "
					+ "reserved no-ops: pagination|summary|statistics|relations|permissions|analytics. "
					+ "Omit include = default docs+totals. See LIST-QUERY.md. "
					+ "Sort fields: name, description, createdAt, updatedAt. "
					+ "Select roots: id, name, description, createdAt, updatedAt.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Paged list, sparse maps, or CountResponse when include=countOnly"),
			@ApiResponse(responseCode = "400", description = "Invalid page, limit, sort, or select"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated")
	})
	public Object list(@ParameterObject PageQuery query) {
		return itemService.findAll(query);
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Get item by id",
			description = "Optional select=id,name,... for sparse fieldset.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Item found"),
			@ApiResponse(responseCode = "400", description = "Invalid select"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated"),
			@ApiResponse(responseCode = "404", description = "Item not found")
	})
	public Object get(
			@PathVariable UUID id,
			@RequestParam(required = false) String select) {
		return itemService.findById(id, select);
	}

	@PostMapping
	@Operation(summary = "Create item")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Item created"),
			@ApiResponse(responseCode = "400", description = "Validation failed"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated"),
			@ApiResponse(responseCode = "409", description = "Duplicate name")
	})
	public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest request) {
		ItemResponse created = itemService.create(request);
		return ResponseEntity.created(URI.create("/api/v1/items/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update item")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Item updated"),
			@ApiResponse(responseCode = "400", description = "Validation failed"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated"),
			@ApiResponse(responseCode = "404", description = "Item not found"),
			@ApiResponse(responseCode = "409", description = "Duplicate name")
	})
	public ItemResponse update(@PathVariable UUID id, @Valid @RequestBody ItemRequest request) {
		return itemService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete item")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Item deleted"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated"),
			@ApiResponse(responseCode = "404", description = "Item not found")
	})
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		itemService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
