package com.kmj5004.hdljudge.playground.dto;

import com.kmj5004.hdljudge.judge.adapter.SimulationOutcome;

public record SimulateResponse(
    SimulationOutcome.Status status,
    String stdout,
    String stderrTail,
    int exitCode,
    long wallTimeMs
) {

    public static SimulateResponse from(SimulationOutcome o) {
        return new SimulateResponse(o.status(), o.stdout(), o.stderrTail(), o.exitCode(), o.wallTimeMs());
    }
}
