package com.kmj5004.hdljudge.domain.challenge;

import com.kmj5004.hdljudge.common.BaseTimeEntity;
import com.kmj5004.hdljudge.common.enums.Difficulty;
import com.kmj5004.hdljudge.common.enums.Language;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "challenges", uniqueConstraints = @UniqueConstraint(name = "uk_challenges_slug", columnNames = "slug"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slug", nullable = false, length = 120)
    private String slug;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 16)
    private Language language;

    @Column(name = "skeleton", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String skeleton;

    @Column(name = "hidden_testbench", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String hiddenTestbench;

    @Column(name = "time_limit_ns", nullable = false)
    private long timeLimitNs;

    @Column(name = "wall_time_limit_ms", nullable = false)
    private int wallTimeLimitMs;

    @Column(name = "memory_limit_mb", nullable = false)
    private int memoryLimitMb;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 16)
    private Difficulty difficulty;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "challenge_tags",
        joinColumns = @JoinColumn(name = "challenge_id", foreignKey = @jakarta.persistence.ForeignKey(name = "fk_challenge_tags_challenge"))
    )
    @Column(name = "tag", nullable = false, length = 40)
    private Set<String> tags = new HashSet<>();

    @Builder
    private Challenge(
        String slug,
        String title,
        String description,
        Language language,
        String skeleton,
        String hiddenTestbench,
        long timeLimitNs,
        int wallTimeLimitMs,
        int memoryLimitMb,
        Difficulty difficulty,
        Set<String> tags
    ) {
        this.slug = slug;
        this.title = title;
        this.description = description;
        this.language = language;
        this.skeleton = skeleton;
        this.hiddenTestbench = hiddenTestbench;
        this.timeLimitNs = timeLimitNs;
        this.wallTimeLimitMs = wallTimeLimitMs;
        this.memoryLimitMb = memoryLimitMb;
        this.difficulty = difficulty;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    public void update(
        String title,
        String description,
        String skeleton,
        String hiddenTestbench,
        long timeLimitNs,
        int wallTimeLimitMs,
        int memoryLimitMb,
        Difficulty difficulty,
        Set<String> tags
    ) {
        this.title = title;
        this.description = description;
        this.skeleton = skeleton;
        this.hiddenTestbench = hiddenTestbench;
        this.timeLimitNs = timeLimitNs;
        this.wallTimeLimitMs = wallTimeLimitMs;
        this.memoryLimitMb = memoryLimitMb;
        this.difficulty = difficulty;
        this.tags.clear();
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    public void softDelete(LocalDateTime now) {
        this.deletedAt = now;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
