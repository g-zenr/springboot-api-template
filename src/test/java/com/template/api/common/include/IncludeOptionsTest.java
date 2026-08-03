package com.template.api.common.include;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class IncludeOptionsTest {

	@Test
	void parse_nullOrBlank_isEmptyAndWantsCount() {
		assertThat(IncludeOptions.parse(null).isEmpty()).isTrue();
		assertThat(IncludeOptions.parse("").isEmpty()).isTrue();
		assertThat(IncludeOptions.parse("   ").isEmpty()).isTrue();
		assertThat(IncludeOptions.parse(null).wantsCount()).isTrue();
	}

	@Test
	void parse_commaSeparated_trimsAndDedupesCaseInsensitive() {
		IncludeOptions options = IncludeOptions.parse(" Count , COUNT, count ,  summary ");
		assertThat(options.has(IncludeKey.COUNT)).isTrue();
		assertThat(options.has(IncludeKey.SUMMARY)).isTrue();
		assertThat(options.keys()).hasSize(2);
	}

	@Test
	void parse_ignoresUnknownTokensWithoutError() {
		IncludeOptions options = IncludeOptions.parse("nope,count,also-unknown");
		assertThat(options.has(IncludeKey.COUNT)).isTrue();
		assertThat(options.keys()).containsExactly(IncludeKey.COUNT);
	}

	@Test
	void wantsCount_falseWhenOtherIncludesWithoutCount() {
		IncludeOptions options = IncludeOptions.parse("summary,analytics");
		assertThat(options.wantsCount()).isFalse();
		assertThat(options.has(IncludeKey.SUMMARY)).isTrue();
	}

	@Test
	void wantsCount_trueWhenCountExplicit() {
		assertThat(IncludeOptions.parse("count").wantsCount()).isTrue();
		assertThat(IncludeOptions.parse("summary,count").wantsCount()).isTrue();
	}

	@Test
	void countOnly_wantsCountButNotContent() {
		IncludeOptions options = IncludeOptions.parse("countOnly");
		assertThat(options.wantsCountOnly()).isTrue();
		assertThat(options.wantsContent()).isFalse();
		assertThat(options.wantsCount()).isTrue();
	}

	@Test
	void countOnly_caseInsensitiveVariants() {
		assertThat(IncludeOptions.parse("countonly").wantsCountOnly()).isTrue();
		assertThat(IncludeOptions.parse("COUNTONLY").wantsCountOnly()).isTrue();
		assertThat(IncludeOptions.parse(" CountOnly ").wantsCountOnly()).isTrue();
	}

	@Test
	void countOnly_combinedWithOtherTokens_stillCountOnly() {
		IncludeOptions options = IncludeOptions.parse("summary,countOnly,count");
		assertThat(options.wantsCountOnly()).isTrue();
		assertThat(options.wantsContent()).isFalse();
		assertThat(options.has(IncludeKey.COUNT)).isTrue();
	}

	@Test
	void default_wantsContent() {
		assertThat(IncludeOptions.parse(null).wantsContent()).isTrue();
		assertThat(IncludeOptions.parse("count").wantsContent()).isTrue();
	}

	@Test
	void raw_preservedForEcho() {
		assertThat(IncludeOptions.parse(" Count ").raw()).isEqualTo("Count");
		assertThat(IncludeOptions.parse(null).raw()).isNull();
	}

	@Test
	void parse_everyCatalogToken_isRecognized() {
		// Every IncludeKey must be parseable — docs catalog and code stay aligned
		String all = Arrays.stream(IncludeKey.values())
				.map(IncludeKey::token)
				.collect(Collectors.joining(","));
		IncludeOptions options = IncludeOptions.parse(all);
		assertThat(options.keys()).containsExactlyInAnyOrder(IncludeKey.values());
		for (IncludeKey key : IncludeKey.values()) {
			assertThat(IncludeOptions.parse(key.token()).has(key))
					.as("token %s", key.token())
					.isTrue();
		}
	}

	@Test
	void parse_eachReservedToken_alone_skipsCountButWantsContent() {
		// Reserved-only include → no COUNT(*), but still return document content
		for (IncludeKey key : IncludeKey.values()) {
			if (key == IncludeKey.COUNT || key == IncludeKey.COUNT_ONLY) {
				continue;
			}
			IncludeOptions options = IncludeOptions.parse(key.token());
			assertThat(options.has(key)).as(key.token()).isTrue();
			assertThat(options.wantsCount()).as(key.token() + " wantsCount").isFalse();
			assertThat(options.wantsContent()).as(key.token() + " wantsContent").isTrue();
			assertThat(options.wantsCountOnly()).as(key.token() + " wantsCountOnly").isFalse();
		}
	}

	@Test
	void recognizedCsv_listsKnownTokens() {
		assertThat(IncludeOptions.parse("count,summary").recognizedCsv()).contains("count", "summary");
		assertThat(IncludeOptions.parse(null).recognizedCsv()).isNull();
	}
}
