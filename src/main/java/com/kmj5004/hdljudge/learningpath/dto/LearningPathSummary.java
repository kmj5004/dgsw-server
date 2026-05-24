package com.kmj5004.hdljudge.learningpath.dto;

import com.kmj5004.hdljudge.common.enums.Difficulty;
import com.kmj5004.hdljudge.common.enums.Language;


public record LearningPathSummary(
    String slug,
    String title,
    String goal,
    Language language,
    Difficulty difficulty,
    Integer estimatedHours,
    int stepCount
) {

    public static LearningPathSummary from(LearningPath p) {
        return new LearningPathSummary(
            p.slug(), p.title(), p.goal(),
            p.language(), p.difficulty(), p.estimatedHours(),
            p.steps().size()
        );
    }
}
