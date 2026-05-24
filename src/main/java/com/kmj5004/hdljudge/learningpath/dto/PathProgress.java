package com.kmj5004.hdljudge.learningpath.dto;

import com.kmj5004.hdljudge.common.enums.Language;


public record PathProgress(
    String slug,
    String title,
    Language language,
    int solvedSteps,
    int totalSteps
) {
}
