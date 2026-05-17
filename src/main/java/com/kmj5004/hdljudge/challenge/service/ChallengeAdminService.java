package com.kmj5004.hdljudge.challenge.service;

import com.kmj5004.hdljudge.challenge.dto.ChallengeCreateRequest;
import com.kmj5004.hdljudge.challenge.dto.ChallengeDetail;
import com.kmj5004.hdljudge.challenge.dto.ChallengeUpdateRequest;
import com.kmj5004.hdljudge.challenge.dto.TestVectorRequest;
import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.domain.challenge.Challenge;
import com.kmj5004.hdljudge.domain.challenge.ChallengeRepository;
import com.kmj5004.hdljudge.domain.challenge.TestVector;
import com.kmj5004.hdljudge.domain.challenge.TestVectorRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChallengeAdminService {

    private final ChallengeRepository challenges;
    private final TestVectorRepository testVectors;

    public ChallengeDetail create(ChallengeCreateRequest req) {
        if (challenges.existsBySlug(req.slug())) {
            throw new ApiException(ErrorCode.SLUG_ALREADY_EXISTS);
        }
        validateOrderingUnique(req.testVectors());

        Challenge challenge = Challenge.builder()
            .slug(req.slug())
            .title(req.title())
            .description(req.description())
            .language(req.language())
            .skeleton(req.skeleton())
            .hiddenTestbench(req.hiddenTestbench())
            .timeLimitNs(req.timeLimitNs())
            .wallTimeLimitMs(req.wallTimeLimitMs())
            .memoryLimitMb(req.memoryLimitMb())
            .difficulty(req.difficulty())
            .tags(req.tags() == null ? Set.of() : req.tags())
            .build();
        Challenge saved = challenges.save(challenge);

        for (TestVectorRequest tv : req.testVectors()) {
            testVectors.save(toEntity(saved, tv));
        }
        return ChallengeDetail.from(saved);
    }

    public ChallengeDetail update(Long id, ChallengeUpdateRequest req) {
        Challenge challenge = challenges.findById(id)
            .filter(c -> !c.isDeleted())
            .orElseThrow(() -> new ApiException(ErrorCode.CHALLENGE_NOT_FOUND));

        challenge.update(
            req.title(),
            req.description(),
            req.skeleton(),
            req.hiddenTestbench(),
            req.timeLimitNs(),
            req.wallTimeLimitMs(),
            req.memoryLimitMb(),
            req.difficulty(),
            req.tags() == null ? Set.of() : req.tags()
        );

        if (req.testVectors() != null) {
            validateOrderingUnique(req.testVectors());

            List<TestVector> existing = testVectors.findByChallengeIdOrderByOrderingAsc(id);
            testVectors.deleteAllInBatch(existing);
            testVectors.flush();
            for (TestVectorRequest tv : req.testVectors()) {
                testVectors.save(toEntity(challenge, tv));
            }
        }

        return ChallengeDetail.from(challenge);
    }

    public void softDelete(Long id) {
        Challenge challenge = challenges.findById(id)
            .filter(c -> !c.isDeleted())
            .orElseThrow(() -> new ApiException(ErrorCode.CHALLENGE_NOT_FOUND));
        challenge.softDelete(LocalDateTime.now());
    }

    private TestVector toEntity(Challenge owner, TestVectorRequest req) {
        return TestVector.builder()
            .challenge(owner)
            .ordering(req.ordering())
            .stimulusJson(req.stimulusJson())
            .expectedJson(req.expectedJson())
            .weight(req.weight())
            .build();
    }

    private void validateOrderingUnique(List<TestVectorRequest> vectors) {
        Set<Integer> seen = new HashSet<>();
        for (TestVectorRequest v : vectors) {
            if (!seen.add(v.ordering())) {
                throw new ApiException(ErrorCode.INVALID_INPUT, "testVectors의 ordering이 중복됩니다: " + v.ordering());
            }
        }
    }
}
