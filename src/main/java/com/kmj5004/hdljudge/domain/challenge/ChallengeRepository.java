package com.kmj5004.hdljudge.domain.challenge;

import com.kmj5004.hdljudge.common.enums.Difficulty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    Optional<Challenge> findBySlug(String slug);

    Optional<Challenge> findBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlug(String slug);

    List<Challenge> findBySlugIn(Collection<String> slugs);

    @Query("""
        SELECT c FROM Challenge c
        WHERE c.deletedAt IS NULL
          AND (:difficulty IS NULL OR c.difficulty = :difficulty)
          AND (:tag IS NULL OR :tag MEMBER OF c.tags)
        """)
    Page<Challenge> findActive(
        @Param("difficulty") Difficulty difficulty,
        @Param("tag") String tag,
        Pageable pageable
    );
}
