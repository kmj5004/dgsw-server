package com.kmj5004.hdljudge.playground.dto;

public record SynthesizeResponse(
    boolean ok,
    boolean cached,
    String svg,
    int gateCount,
    int ffCount,
    Integer maxGateCount,
    String stderrTail
) {
}
