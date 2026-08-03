package com.template.api.common.select;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class FieldProjectorTest {

	@Test
	void project_topLevelFields() {
		Map<String, Object> source = Map.of("id", "1", "name", "Widget", "description", "x");
		Map<String, Object> projected = FieldProjector.project(source, List.of("id", "name"));

		assertThat(projected).containsOnlyKeys("id", "name");
		assertThat(projected.get("name")).isEqualTo("Widget");
	}

	@Test
	void project_deepPaths() {
		Map<String, Object> owner = new LinkedHashMap<>();
		owner.put("username", "alice");
		owner.put("roles", List.of("USER"));
		Map<String, Object> source = new LinkedHashMap<>();
		source.put("id", "n1");
		source.put("title", "Hello");
		source.put("owner", owner);

		Map<String, Object> projected =
				FieldProjector.project(source, List.of("id", "owner.username", "owner.roles"));

		assertThat(projected).containsKeys("id", "owner");
		@SuppressWarnings("unchecked")
		Map<String, Object> nested = (Map<String, Object>) projected.get("owner");
		assertThat(nested).containsEntry("username", "alice");
		assertThat(nested).containsKey("roles");
		assertThat(nested).doesNotContainKey("id");
	}

	@Test
	void project_deeperNesting() {
		Map<String, Object> profile = Map.of("firstname", "Ada");
		Map<String, Object> user = Map.of("profile", profile);
		Map<String, Object> source = Map.of("user", user);

		Map<String, Object> projected = FieldProjector.project(source, List.of("user.profile.firstname"));

		@SuppressWarnings("unchecked")
		Map<String, Object> u = (Map<String, Object>) projected.get("user");
		@SuppressWarnings("unchecked")
		Map<String, Object> p = (Map<String, Object>) u.get("profile");
		assertThat(p).containsEntry("firstname", "Ada");
	}

	@Test
	void selectPaths_parseAndValidate() {
		List<String> paths = SelectPaths.parse(" id, name , owner.username ");
		assertThat(paths).containsExactly("id", "name", "owner.username");

		SelectPaths.validateRoots(paths, Set.of("id", "name", "owner"));
		assertThat(SelectPaths.requestsRoot(paths, "owner")).isTrue();
		assertThat(SelectPaths.requestsRoot(paths, "body")).isFalse();
	}

	@Test
	void selectPaths_rejectsUnknownRoot() {
		assertThatThrownBy(() -> SelectPaths.validateRoots(List.of("hacked"), Set.of("id", "name")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid select field");
	}

	@Test
	void selectPaths_rejectsBadSyntax() {
		assertThatThrownBy(() -> SelectPaths.parse("owner..username"))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
