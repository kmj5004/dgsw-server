package com.kmj5004.hdljudge.domain.submission;

import com.kmj5004.hdljudge.common.enums.SubmissionStatus;
import com.kmj5004.hdljudge.domain.challenge.Challenge;
import com.kmj5004.hdljudge.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "submissions")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_submissions_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_submissions_challenge"))
    private Challenge challenge;

    @Column(name = "code", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubmissionStatus status;

    @Column(name = "score", nullable = false)
    private int score;

    @CreatedDate
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    private Submission(User user, Challenge challenge, String code) {
        this.user = user;
        this.challenge = challenge;
        this.code = code;
        this.status = SubmissionStatus.PENDING;
        this.score = 0;
    }

    public void markRunning() {
        this.status = SubmissionStatus.RUNNING;
    }

    public void complete(int score, LocalDateTime now) {
        this.status = SubmissionStatus.COMPLETED;
        this.score = score;
        this.completedAt = now;
    }

    public void fail(SubmissionStatus terminalStatus, LocalDateTime now) {
        if (terminalStatus == SubmissionStatus.COMPLETED || terminalStatus == SubmissionStatus.PENDING || terminalStatus == SubmissionStatus.RUNNING) {
            throw new IllegalArgumentException("Terminal failure status expected, got: " + terminalStatus);
        }
        this.status = terminalStatus;
        this.completedAt = now;
    }
}
