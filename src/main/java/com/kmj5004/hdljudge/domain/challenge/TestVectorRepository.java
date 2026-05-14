package com.kmj5004.hdljudge.domain.challenge;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestVectorRepository extends JpaRepository<TestVector, Long> {

    List<TestVector> findByChallengeIdOrderByOrderingAsc(Long challengeId);
}
