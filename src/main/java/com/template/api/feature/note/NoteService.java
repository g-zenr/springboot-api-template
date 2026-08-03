package com.template.api.feature.note;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.template.api.common.error.ResourceNotFoundException;
import com.template.api.common.paging.PageQuery;
import com.template.api.common.paging.PageResponse;
import com.template.api.common.paging.Pageables;
import com.template.api.common.paging.PagedReads;
import com.template.api.common.select.FieldProjector;
import com.template.api.common.select.FieldSelection;
import com.template.api.common.select.SelectPaths;
import com.template.api.feature.user.AppUserRepository;
import com.template.api.feature.user.UserResponse;

@Service
@Transactional
public class NoteService {

	static final Set<String> SORT_FIELDS = Set.of("title", "body", "createdAt", "updatedAt");
	/** Flat fields plus nested {@code owner} for deep select / populate. */
	static final Set<String> SELECT_ROOTS =
			Set.of("id", "ownerUsername", "title", "body", "createdAt", "updatedAt", "owner");
	static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "updatedAt");

	private final NoteRepository noteRepository;
	private final NoteMapper noteMapper;
	private final AppUserRepository appUserRepository;
	private final PagedReads pagedReads;

	public NoteService(
			NoteRepository noteRepository,
			NoteMapper noteMapper,
			AppUserRepository appUserRepository,
			PagedReads pagedReads) {
		this.noteRepository = noteRepository;
		this.noteMapper = noteMapper;
		this.appUserRepository = appUserRepository;
		this.pagedReads = pagedReads;
	}

	@Transactional(readOnly = true)
	public Object listMine(String username, PageQuery query) {
		Specification<Note> spec = NoteSpecs.ownedBy(username);
		if (query.normalizedSearch().isPresent()) {
			spec = spec.and(NoteSpecs.matchesSearch(query.normalizedSearch().get()));
		}
		if (query.includes().wantsCountOnly()) {
			return pagedReads.countOnly(Note.class, spec);
		}
		Pageable pageable = Pageables.toPageable(query, SORT_FIELDS, DEFAULT_SORT);
		PageResponse<NoteResponse> page =
				pagedReads.find(Note.class, spec, pageable, query, noteMapper::toResponse);

		List<String> paths = SelectPaths.parse(query.getSelect());
		if (paths.isEmpty()) {
			return page;
		}
		SelectPaths.validateRoots(paths, SELECT_ROOTS);

		boolean needOwner = SelectPaths.requestsRoot(paths, "owner");
		Map<String, UserResponse> ownersByUsername = needOwner
				? loadOwners(page.content().stream().map(NoteResponse::ownerUsername).distinct().toList())
				: Map.of();

		return FieldSelection.applyEnriched(page, query, SELECT_ROOTS, note -> enrich(note, ownersByUsername));
	}

	@Transactional(readOnly = true)
	public Object getMine(UUID id, String username, String select) {
		NoteResponse note = noteMapper.toResponse(requireOwned(id, username));
		List<String> paths = SelectPaths.parse(select);
		if (paths.isEmpty()) {
			return note;
		}
		SelectPaths.validateRoots(paths, SELECT_ROOTS);
		Map<String, UserResponse> owners = SelectPaths.requestsRoot(paths, "owner")
				? loadOwners(List.of(note.ownerUsername()))
				: Map.of();
		return FieldProjector.project(enrich(note, owners), paths);
	}

	@Transactional(readOnly = true)
	public NoteResponse getMine(UUID id, String username) {
		return noteMapper.toResponse(requireOwned(id, username));
	}

	public NoteResponse create(String username, NoteRequest request) {
		Note note = noteMapper.toNewEntity(username, request);
		return noteMapper.toResponse(noteRepository.save(note));
	}

	public NoteResponse update(UUID id, String username, NoteRequest request) {
		Note note = requireOwned(id, username);
		note.setTitle(request.title().trim());
		note.setBody(noteMapper.trimToNull(request.body()));
		return noteMapper.toResponse(noteRepository.save(note));
	}

	public void delete(UUID id, String username) {
		Note note = requireOwned(id, username);
		noteRepository.delete(note);
	}

	private Note requireOwned(UUID id, String username) {
		return noteRepository.findByIdAndOwnerUsername(id, username)
				.orElseThrow(() -> new ResourceNotFoundException("Note not found: " + id));
	}

	private Map<String, UserResponse> loadOwners(List<String> usernames) {
		if (usernames.isEmpty()) {
			return Map.of();
		}
		Map<String, UserResponse> map = new HashMap<>();
		for (String name : usernames) {
			appUserRepository.findByUsernameIgnoreCase(name)
					.map(UserResponse::from)
					.ifPresent(u -> map.put(name, u));
		}
		return map;
	}

	/**
	 * Full note map plus optional nested {@code owner} for deep select paths.
	 */
	private static Map<String, Object> enrich(NoteResponse note, Map<String, UserResponse> ownersByUsername) {
		Map<String, Object> full = new LinkedHashMap<>(FieldProjector.toMap(note));
		UserResponse owner = ownersByUsername.get(note.ownerUsername());
		if (owner != null) {
			full.put("owner", FieldProjector.toMap(owner));
		}
		return full;
	}
}
