package com.kmj5004.hdljudge.domain.leaderboard;

import com.kmj5004.hdljudge.domain.challenge.Challenge;
import com.kmj5004.hdljudge.domain.submission.Submission;
import com.kmj5004.hdljudge.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(
    name = "leaderboard_entries",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_leaderboard_challenge_user",
        columnNames = {"challenge_id", "user_id"}
    )
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaderboardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_leaderboard_challenge"))
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_leaderboard_user"))
    private User user;

    @Column(name = "best_score", nullable = false)
    private int bestScore;

    @Column(name = "best_gate_count")
    private Integer bestGateCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "best_submission_id", foreignKey = @jakarta.persistence.ForeignKey(name = "fk_leaderboard_best_submission"))
    private Submission bestSubmission;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private LeaderboardEntry(Challenge challenge, User user, int bestScore, Integer bestGateCount, Submission bestSubmission) {
        this.challenge = challenge;
        this.user = user;
        this.bestScore = bestScore;
        this.bestGateCount = bestGateCount;
        this.bestSubmission = bestSubmission;
    }

    public boolean updateIfBetter(int score, Integer gateCount, Submission submission) {
        boolean scoreImproved = score > this.bestScore;
        boolean gateImproved = score == this.bestScore
            && this.bestGateCount != null
            && gateCount != null
            && gateCount < this.bestGateCount;

        if (scoreImproved || gateImproved) {
            this.bestScore = score;
            this.bestGateCount = gateCount;
            this.bestSubmission = submission;
            return true;
        }
        return false;
    }
}
