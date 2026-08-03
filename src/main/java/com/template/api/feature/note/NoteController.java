package com.template.api.feature.note;

import java.net.URI;
import java.util.UUID;
import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import com.template.api.common.paging.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/notes")
@Tag(name = "Notes", description = "Owner-scoped notes domain sample (Bearer JWT)")
public class NoteController {

	private final NoteService noteService;

	public NoteController(NoteService noteService) {
		this.noteService = noteService;
	}

	@GetMapping
	@Operation(
			summary = "List my notes (paginated)",
			description = "Owner-scoped. Query: page, limit, sort, search (title/body), select, include. "
					+ "Sort fields: title, body, createdAt, updatedAt. Default sort: -updatedAt. "
					+ "Select roots: id, ownerUsername, title, body, createdAt, updatedAt, owner. "
					+ "Deep select populates nested owner: select=id,title,owner.username,owner.roles. "
					+ "include values: count (docs+totals), countOnly ({count} only; mine), "
					+ "reserved: pagination|summary|statistics|relations|permissions|analytics. "
					+ "Omit include = default docs+totals.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Paged notes, sparse maps, or CountResponse when include=countOnly"),
			@ApiResponse(responseCode = "400", description = "Invalid page, limit, sort, or select"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated")
	})
	public Object list(
			@ParameterObject PageQuery query,
			Authentication authentication) {
		return noteService.listMine(authentication.getName(), query);
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Get my note by id",
			description = "Optional select; deep owner paths populate the related user, e.g. owner.username")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Note found"),
			@ApiResponse(responseCode = "400", description = "Invalid select"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated"),
			@ApiResponse(responseCode = "404", description = "Not found or not owned by current user")
	})
	public Object get(
			@PathVariable UUID id,
			@RequestParam(required = false) String select,
			Authentication authentication) {
		return noteService.getMine(id, authentication.getName(), select);
	}

	@PostMapping
	@Operation(summary = "Create a note")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Note created"),
			@ApiResponse(responseCode = "400", description = "Validation failed"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated")
	})
	public ResponseEntity<NoteResponse> create(
			@Valid @RequestBody NoteRequest request,
			Authentication authentication) {
		NoteResponse created = noteService.create(authentication.getName(), request);
		return ResponseEntity.created(URI.create("/api/v1/notes/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update my note")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Note updated"),
			@ApiResponse(responseCode = "400", description = "Validation failed"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated"),
			@ApiResponse(responseCode = "404", description = "Not found or not owned")
	})
	public NoteResponse update(
			@PathVariable UUID id,
			@Valid @RequestBody NoteRequest request,
			Authentication authentication) {
		return noteService.update(id, authentication.getName(), request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete my note")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Note deleted"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated"),
			@ApiResponse(responseCode = "404", description = "Not found or not owned")
	})
	public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
		noteService.delete(id, authentication.getName());
		return ResponseEntity.noContent().build();
	}
}
