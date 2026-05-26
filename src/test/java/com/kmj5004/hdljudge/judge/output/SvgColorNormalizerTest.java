package com.kmj5004.hdljudge.judge.output;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SvgColorNormalizerTest {

    @Test
    void replacesShortBlackHex() {
        String input = "<svg><style>svg { stroke:#000; fill:#000; }</style></svg>";
        String out = SvgColorNormalizer.relaxBlack(input);
        assertThat(out).doesNotContain("#000").contains("currentColor");
    }

    @Test
    void replacesLongBlackHex() {
        String input = "<style>svg { stroke:#000000; }</style>";
        String out = SvgColorNormalizer.relaxBlack(input);
        assertThat(out).doesNotContain("#000000").contains("currentColor");
    }

    @Test
    void leavesOtherColorsAlone() {
        String input = "<style>svg { stroke:#0001; fill:#00ff00; bg:#0008; }</style>";
        String out = SvgColorNormalizer.relaxBlack(input);


        assertThat(out).contains("#0001");
        assertThat(out).contains("#00ff00");
        assertThat(out).contains("#0008");
    }

    @Test
    void preservesNonBlackPrefixedHexes() {

        String input = "fill:#0000aa";
        assertThat(SvgColorNormalizer.relaxBlack(input)).isEqualTo("fill:#0000aa");
    }

    @Test
    void nullAndEmptyArePassedThrough() {
        assertThat(SvgColorNormalizer.relaxBlack(null)).isNull();
        assertThat(SvgColorNormalizer.relaxBlack("")).isEmpty();
    }

    @Test
    void replacesMultipleOccurrences() {
        String input = "stroke:#000;fill:#000;color:#000";
        String out = SvgColorNormalizer.relaxBlack(input);

        assertThat(out).isEqualTo("stroke:currentColor;fill:currentColor;color:currentColor");
    }
}
