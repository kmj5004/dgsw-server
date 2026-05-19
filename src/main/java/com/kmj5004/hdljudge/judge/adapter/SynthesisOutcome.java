package com.kmj5004.hdljudge.judge.adapter;

public record SynthesisOutcome(
    boolean ok,
    String svg,
    String json,
    int gateCount,
    int ffCount,
    String stderrTail
) {

    public static SynthesisOutcome failure(String stderrTail) {
        return new SynthesisOutcome(false, null, null, 0, 0, stderrTail);
    }

    public static SynthesisOutcome success(String svg, String json, int gateCount, int ffCount) {
        return new SynthesisOutcome(true, svg, json, gateCount, ffCount, null);
    }
}
