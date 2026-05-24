package com.kmj5004.hdljudge.learningpath.dto;

public record StepProgress(
    int ordering,
    String challengeSlug,
    String title,
    String rationale,
    int bestScore,
    boolean attempted,
    boolean solved
) {

    public static StepProgress of(LearningPathStep step, int bestScore, boolean attempted) {
        return new StepProgress(
            step.ordering(), step.challengeSlug(), step.title(), step.rationale(),
            bestScore, attempted, bestScore >= 100
        );
    }
}
