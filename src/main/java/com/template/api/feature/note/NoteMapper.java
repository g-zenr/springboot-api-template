package com.template.api.feature.note;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface NoteMapper {

	NoteResponse toResponse(Note note);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "ownerUsername", source = "ownerUsername")
	@Mapping(target = "title", source = "request.title", qualifiedByName = "trim")
	@Mapping(target = "body", source = "request.body", qualifiedByName = "trimToNull")
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	Note toNewEntity(String ownerUsername, NoteRequest request);

	@Named("trim")
	default String trim(String value) {
		return value == null ? null : value.trim();
	}

	@Named("trimToNull")
	default String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
