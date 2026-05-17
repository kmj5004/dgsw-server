package com.kmj5004.hdljudge.challenge.service;

import com.kmj5004.hdljudge.challenge.dto.ChallengeDetail;
import com.kmj5004.hdljudge.challenge.dto.ChallengeSummary;
import com.kmj5004.hdljudge.common.enums.Difficulty;
import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.common.web.PageResponse;
import com.kmj5004.hdljudge.domain.challenge.Challenge;
import com.kmj5004.hdljudge.domain.challenge.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChallengeQueryService {

    private final ChallengeRepository challenges;

    public PageResponse<ChallengeSummary> list(Difficulty difficulty, String tag, Pageable pageable) {
        return PageResponse.of(challenges.findActive(difficulty, tag, pageable), ChallengeSummary::from);
    }

    public ChallengeDetail getBySlug(String slug) {
        Challenge challenge = challenges.findBySlugAndDeletedAtIsNull(slug)
            .orElseThrow(() -> new ApiException(ErrorCode.CHALLENGE_NOT_FOUND));
        return ChallengeDetail.from(challenge);
    }
}
