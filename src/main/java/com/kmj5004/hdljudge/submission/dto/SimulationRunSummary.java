package com.kmj5004.hdljudge.submission.dto;

import com.kmj5004.hdljudge.domain.submission.SimulationRun;

public record SimulationRunSummary(
    int ordering,
    boolean passed,
    String actualOutputJson,
    Integer wallTimeMs
) {

    public static SimulationRunSummary from(SimulationRun run) {
        return new SimulationRunSummary(
            run.getTestVector().getOrdering(),
            run.isPassed(),
            run.getActualOutputJson(),
            run.getWallTimeMs()
        );
    }
}
