package com.kmj5004.hdljudge.learningpath.dto;

import com.kmj5004.hdljudge.common.enums.Difficulty;
import com.kmj5004.hdljudge.common.enums.Language;
import java.util.List;

public record LearningPath(
    String slug,
    String title,
    String description,
    String goal,
    Language language,
    Difficulty difficulty,
    Integer estimatedHours,
    List<LearningPathStep> steps
) {
}
