package com.kmj5004.hdljudge.domain.submission;

import com.kmj5004.hdljudge.domain.challenge.TestVector;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "simulation_runs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SimulationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_simulation_runs_submission"))
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_vector_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_simulation_runs_test_vector"))
    private TestVector testVector;

    @Column(name = "passed", nullable = false)
    private boolean passed;

    @Column(name = "actual_output_json", columnDefinition = "MEDIUMTEXT")
    private String actualOutputJson;

    @Column(name = "simulation_time_ns")
    private Long simulationTimeNs;

    @Column(name = "wall_time_ms")
    private Integer wallTimeMs;

    @Column(name = "stderr_tail", columnDefinition = "MEDIUMTEXT")
    private String stderrTail;

    @Builder
    private SimulationRun(
        Submission submission,
        TestVector testVector,
        boolean passed,
        String actualOutputJson,
        Long simulationTimeNs,
        Integer wallTimeMs,
        String stderrTail
    ) {
        this.submission = submission;
        this.testVector = testVector;
        this.passed = passed;
        this.actualOutputJson = actualOutputJson;
        this.simulationTimeNs = simulationTimeNs;
        this.wallTimeMs = wallTimeMs;
        this.stderrTail = stderrTail;
    }
}
