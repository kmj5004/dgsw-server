package com.kmj5004.hdljudge.domain.synthesis;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SynthesisResultRepository extends JpaRepository<SynthesisResult, Long> {

    Optional<SynthesisResult> findByCodeHash(String codeHash);
}
