package com.kmj5004.hdljudge.domain.submission;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Page<Submission> findByUserId(Long userId, Pageable pageable);

    Page<Submission> findByUserIdAndChallengeId(Long userId, Long challengeId, Pageable pageable);

    List<Submission> findTop20ByUserIdOrderBySubmittedAtDesc(Long userId);

    List<Submission> findTop5ByUserIdOrderBySubmittedAtDesc(Long userId);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(DISTINCT s.challenge.id) FROM Submission s WHERE s.user.id = :userId")
    long countDistinctChallengeIdByUserId(@Param("userId") Long userId);
}
