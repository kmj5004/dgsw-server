package com.kmj5004.hdljudge.learningpath.dto;

public record LearningPathStep(
    int ordering,
    String challengeSlug,
    String title,
    String rationale
) {
}
