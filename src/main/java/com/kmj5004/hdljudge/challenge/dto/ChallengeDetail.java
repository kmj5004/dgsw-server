package com.kmj5004.hdljudge.challenge.dto;

import com.kmj5004.hdljudge.common.enums.Difficulty;
import com.kmj5004.hdljudge.common.enums.Language;
import com.kmj5004.hdljudge.domain.challenge.Challenge;
import java.util.HashSet;
import java.util.Set;




public record ChallengeDetail(
    Long id,
    String slug,
    String title,
    String description,
    Language language,
    String skeleton,
    Difficulty difficulty,
    long timeLimitNs,
    int wallTimeLimitMs,
    int memoryLimitMb,
    Set<String> tags
) {

    public static ChallengeDetail from(Challenge c) {
        return new ChallengeDetail(
            c.getId(),
            c.getSlug(),
            c.getTitle(),
            c.getDescription(),
            c.getLanguage(),
            c.getSkeleton(),
            c.getDifficulty(),
            c.getTimeLimitNs(),
            c.getWallTimeLimitMs(),
            c.getMemoryLimitMb(),
            new HashSet<>(c.getTags())
        );
    }
}
