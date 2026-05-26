package com.kmj5004.hdljudge.judge.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import org.junit.jupiter.api.Test;

class OutputParserTest {

    private final OutputParser parser = new OutputParser();

    @Test
    void parsesAllVectorsInOrder() {
        String stdout = """
            ::HDLJUDGE_VEC::ordering=1::output={"sum":0,"carry":0}
            ::HDLJUDGE_VEC::ordering=2::output={"sum":1,"carry":0}
            ::HDLJUDGE_VEC::ordering=3::output={"sum":1,"carry":0}
            ::HDLJUDGE_VEC::ordering=4::output={"sum":0,"carry":1}
            """;

        Map<Integer, String> result = parser.parse(stdout);

        assertThat(result).containsExactly(
            entry(1, "{\"sum\":0,\"carry\":0}"),
            entry(2, "{\"sum\":1,\"carry\":0}"),
            entry(3, "{\"sum\":1,\"carry\":0}"),
            entry(4, "{\"sum\":0,\"carry\":1}")
        );
    }

    @Test
    void ignoresUnrelatedLines() {
        String stdout = """
            VCD info: opening hello.vcd
            ::HDLJUDGE_VEC::ordering=1::output={"y":1}
            user $display said hi
            ::HDLJUDGE_VEC::ordering=2::output={"y":0}
            """;

        Map<Integer, String> result = parser.parse(stdout);

        assertThat(result).hasSize(2);
        assertThat(result.get(1)).isEqualTo("{\"y\":1}");
        assertThat(result.get(2)).isEqualTo("{\"y\":0}");
    }

    @Test
    void emptyStdoutReturnsEmptyMap() {
        assertThat(parser.parse("")).isEmpty();
        assertThat(parser.parse(null)).isEmpty();
    }

    @Test
    void duplicateOrderingKeepsLastOccurrence() {
        String stdout = """
            ::HDLJUDGE_VEC::ordering=1::output={"y":0}
            ::HDLJUDGE_VEC::ordering=1::output={"y":1}
            """;

        Map<Integer, String> result = parser.parse(stdout);

        assertThat(result).hasSize(1);
        assertThat(result.get(1)).isEqualTo("{\"y\":1}");
    }

    @Test
    void malformedMarkerLineDoesNotCrash() {
        String stdout = """
            ::HDLJUDGE_VEC:::not what we want
            ::HDLJUDGE_VEC::ordering=abc::output={"y":1}
            ::HDLJUDGE_VEC::ordering=2::output={"y":0}
            """;

        Map<Integer, String> result = parser.parse(stdout);


        assertThat(result).hasSize(1).containsKey(2);
    }

    @Test
    void surroundingWhitespaceIsTolerated() {
        String stdout = "   ::HDLJUDGE_VEC::ordering=7::output={\"y\":1}   \n";

        Map<Integer, String> result = parser.parse(stdout);

        assertThat(result).containsExactly(entry(7, "{\"y\":1}"));
    }
}
