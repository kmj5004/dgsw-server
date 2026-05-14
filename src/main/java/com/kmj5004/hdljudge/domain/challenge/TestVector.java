package com.kmj5004.hdljudge.domain.challenge;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "test_vectors",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_test_vectors_challenge_ordering",
        columnNames = {"challenge_id", "ordering"}
    )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestVector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_test_vectors_challenge"))
    private Challenge challenge;

    @Column(name = "ordering", nullable = false)
    private int ordering;

    @Column(name = "stimulus_json", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String stimulusJson;

    @Column(name = "expected_json", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String expectedJson;

    @Column(name = "weight", nullable = false)
    private int weight;

    @Builder
    private TestVector(Challenge challenge, int ordering, String stimulusJson, String expectedJson, int weight) {
        this.challenge = challenge;
        this.ordering = ordering;
        this.stimulusJson = stimulusJson;
        this.expectedJson = expectedJson;
        this.weight = weight;
    }
}
