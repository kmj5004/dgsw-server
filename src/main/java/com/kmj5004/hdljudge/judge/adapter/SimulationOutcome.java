package com.kmj5004.hdljudge.judge.adapter;

public record SimulationOutcome(
    Status status,
    String stdout,
    String stderrTail,
    int exitCode,
    long wallTimeMs
) {

    public enum Status {
        OK,
        TIMEOUT,
        ERROR
    }
}
