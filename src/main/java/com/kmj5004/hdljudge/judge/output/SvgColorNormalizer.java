package com.kmj5004.hdljudge.judge.output;







public final class SvgColorNormalizer {

    private SvgColorNormalizer() {}

    public static String relaxBlack(String svg) {
        if (svg == null || svg.isEmpty()) {
            return svg;
        }

        String result = svg.replaceAll("#000000(?![0-9a-fA-F])", "currentColor");
        result = result.replaceAll("#000(?![0-9a-fA-F])", "currentColor");
        return result;
    }
}
