package com.kmj5004.hdljudge.challenge.dto;

import com.kmj5004.hdljudge.common.enums.Difficulty;
import com.kmj5004.hdljudge.common.enums.Language;
import com.kmj5004.hdljudge.domain.challenge.Challenge;
import java.util.HashSet;
import java.util.Set;

public record ChallengeSummary(
    Long id,
    String slug,
    String title,
    Language language,
    Difficulty difficulty,
    Set<String> tags
) {

    public static ChallengeSummary from(Challenge c) {
        return new ChallengeSummary(
            c.getId(),
            c.getSlug(),
            c.getTitle(),
            c.getLanguage(),
            c.getDifficulty(),
            new HashSet<>(c.getTags())
        );
    }
}
