package com.template.api.feature.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ItemMapper {

	ItemResponse toResponse(Item item);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "name", source = "name", qualifiedByName = "trim")
	@Mapping(target = "description", source = "description", qualifiedByName = "trimToNull")
	Item toNewEntity(ItemRequest request);

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
