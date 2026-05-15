package com.kmj5004.hdljudge.domain.submission;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationRunRepository extends JpaRepository<SimulationRun, Long> {

    List<SimulationRun> findBySubmissionId(Long submissionId);
}
