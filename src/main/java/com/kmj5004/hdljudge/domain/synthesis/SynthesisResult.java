package com.kmj5004.hdljudge.domain.synthesis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(
    name = "synthesis_results",
    uniqueConstraints = @UniqueConstraint(name = "uk_synthesis_results_code_hash", columnNames = "code_hash")
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SynthesisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Column(name = "svg_content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String svgContent;

    @Column(name = "gate_count", nullable = false)
    private int gateCount;

    @Column(name = "ff_count", nullable = false)
    private int ffCount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private SynthesisResult(String codeHash, String svgContent, int gateCount, int ffCount) {
        this.codeHash = codeHash;
        this.svgContent = svgContent;
        this.gateCount = gateCount;
        this.ffCount = ffCount;
    }
}
