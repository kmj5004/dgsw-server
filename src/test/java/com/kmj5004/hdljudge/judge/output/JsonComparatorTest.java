package com.kmj5004.hdljudge.judge.output;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class JsonComparatorTest {

    private final JsonComparator comparator = new JsonComparator(new ObjectMapper());

    @Test
    void identicalJsonStringsAreEqual() {
        assertThat(comparator.equalsJson("{\"a\":1}", "{\"a\":1}")).isTrue();
    }

    @Test
    void keyOrderDoesNotMatter() {
        assertThat(comparator.equalsJson(
            "{\"a\":1,\"b\":2}",
            "{\"b\":2,\"a\":1}"
        )).isTrue();
    }

    @Test
    void whitespaceDoesNotMatter() {
        assertThat(comparator.equalsJson(
            "{\"a\":1, \"b\":2}",
            "{\"a\":1,\"b\":2}"
        )).isTrue();
    }

    @Test
    void differentValuesAreNotEqual() {
        assertThat(comparator.equalsJson("{\"a\":1}", "{\"a\":2}")).isFalse();
    }

    @Test
    void missingKeyMakesNotEqual() {
        assertThat(comparator.equalsJson("{\"a\":1,\"b\":2}", "{\"a\":1}")).isFalse();
    }

    @Test
    void nullsAreNotEqual() {
        assertThat(comparator.equalsJson(null, "{\"a\":1}")).isFalse();
        assertThat(comparator.equalsJson("{\"a\":1}", null)).isFalse();
        assertThat(comparator.equalsJson(null, null)).isFalse();
    }

    @Test
    void malformedJsonReturnsFalseDoesNotThrow() {
        assertThat(comparator.equalsJson("not json", "{\"a\":1}")).isFalse();
        assertThat(comparator.equalsJson("{\"a\":1}", "not json")).isFalse();
    }

    @Test
    void numberVsStringIsNotEqual() {
        assertThat(comparator.equalsJson("{\"a\":1}", "{\"a\":\"1\"}")).isFalse();
    }
}
