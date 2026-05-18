package com.kmj5004.hdljudge.submission;

import com.kmj5004.hdljudge.common.enums.Role;
import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.common.web.PageResponse;
import com.kmj5004.hdljudge.domain.challenge.Challenge;
import com.kmj5004.hdljudge.domain.challenge.ChallengeRepository;
import com.kmj5004.hdljudge.domain.submission.SimulationRun;
import com.kmj5004.hdljudge.domain.submission.SimulationRunRepository;
import com.kmj5004.hdljudge.domain.submission.Submission;
import com.kmj5004.hdljudge.domain.submission.SubmissionRepository;
import com.kmj5004.hdljudge.domain.user.User;
import com.kmj5004.hdljudge.domain.user.UserRepository;
import com.kmj5004.hdljudge.judge.JudgeOrchestrator;
import com.kmj5004.hdljudge.security.AuthPrincipal;
import com.kmj5004.hdljudge.submission.dto.SubmissionAccepted;
import com.kmj5004.hdljudge.submission.dto.SubmissionDetail;
import com.kmj5004.hdljudge.submission.dto.SubmissionListItem;
import com.kmj5004.hdljudge.submission.dto.SubmissionRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissions;
    private final SimulationRunRepository simulationRuns;
    private final UserRepository users;
    private final ChallengeRepository challenges;
    private final JudgeOrchestrator orchestrator;

    @Transactional
    public SubmissionAccepted submit(AuthPrincipal principal, SubmissionRequest req) {
        User user = users.findById(principal.userId())
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Challenge challenge = challenges.findBySlugAndDeletedAtIsNull(req.challengeSlug())
            .orElseThrow(() -> new ApiException(ErrorCode.CHALLENGE_NOT_FOUND));

        Submission saved = submissions.save(Submission.builder()
            .user(user)
            .challenge(challenge)
            .code(req.code())
            .build());
        Long submissionId = saved.getId();


        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    orchestrator.enqueue(submissionId);
                } catch (RuntimeException e) {

                    log.warn("Failed to enqueue submission {} after commit: {}", submissionId, e.toString());
                }
            }
        });

        return new SubmissionAccepted(submissionId, saved.getStatus());
    }

    @Transactional(readOnly = true)
    public SubmissionDetail get(AuthPrincipal principal, Long id) {
        Submission sub = submissions.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.SUBMISSION_NOT_FOUND));
        ensureCanRead(principal, sub);

        List<SimulationRun> runs = simulationRuns.findBySubmissionId(id);
        return SubmissionDetail.from(sub, runs);
    }

    @Transactional(readOnly = true)
    public PageResponse<SubmissionListItem> listMine(AuthPrincipal principal, Long challengeId, Pageable pageable) {
        if (challengeId != null) {
            return PageResponse.of(
                submissions.findByUserIdAndChallengeId(principal.userId(), challengeId, pageable),
                SubmissionListItem::from
            );
        }
        return PageResponse.of(
            submissions.findByUserId(principal.userId(), pageable),
            SubmissionListItem::from
        );
    }

    private void ensureCanRead(AuthPrincipal principal, Submission sub) {
        if (principal.role() == Role.ADMIN) {
            return;
        }
        if (!sub.getUser().getId().equals(principal.userId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }
}
