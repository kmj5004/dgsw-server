



CREATE TABLE users (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(16)  NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    token_hash  VARCHAR(128) NOT NULL,
    expires_at  DATETIME(6)  NOT NULL,
    rotated_at  DATETIME(6)  NULL,
    created_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token_hash (token_hash),
    KEY idx_refresh_tokens_user (user_id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE challenges (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    slug                 VARCHAR(120)  NOT NULL,
    title                VARCHAR(255)  NOT NULL,
    description          MEDIUMTEXT    NOT NULL,
    language             VARCHAR(16)   NOT NULL,
    skeleton             MEDIUMTEXT    NOT NULL,
    hidden_testbench     MEDIUMTEXT    NOT NULL,
    time_limit_ns        BIGINT        NOT NULL,
    wall_time_limit_ms   INT           NOT NULL,
    memory_limit_mb      INT           NOT NULL,
    difficulty           VARCHAR(16)   NOT NULL,
    deleted_at           DATETIME(6)   NULL,
    created_at           DATETIME(6)   NOT NULL,
    updated_at           DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_challenges_slug (slug),
    KEY idx_challenges_difficulty (difficulty),
    KEY idx_challenges_deleted_at (deleted_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE challenge_tags (
    challenge_id BIGINT       NOT NULL,
    tag          VARCHAR(40)  NOT NULL,
    PRIMARY KEY (challenge_id, tag),
    KEY idx_challenge_tags_tag (tag),
    CONSTRAINT fk_challenge_tags_challenge FOREIGN KEY (challenge_id) REFERENCES challenges (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE test_vectors (
    id            BIGINT     NOT NULL AUTO_INCREMENT,
    challenge_id  BIGINT     NOT NULL,
    ordering      INT        NOT NULL,
    stimulus_json MEDIUMTEXT NOT NULL,
    expected_json MEDIUMTEXT NOT NULL,
    weight        INT        NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_test_vectors_challenge_ordering (challenge_id, ordering),
    CONSTRAINT fk_test_vectors_challenge FOREIGN KEY (challenge_id) REFERENCES challenges (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE submissions (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    user_id       BIGINT      NOT NULL,
    challenge_id  BIGINT      NOT NULL,
    code          MEDIUMTEXT  NOT NULL,
    status        VARCHAR(20) NOT NULL,
    score         INT         NOT NULL DEFAULT 0,
    submitted_at  DATETIME(6) NOT NULL,
    completed_at  DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_submissions_user (user_id),
    KEY idx_submissions_challenge (challenge_id),
    KEY idx_submissions_status (status),
    KEY idx_submissions_submitted_at (submitted_at),
    CONSTRAINT fk_submissions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_challenge FOREIGN KEY (challenge_id) REFERENCES challenges (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE simulation_runs (
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    submission_id       BIGINT      NOT NULL,
    test_vector_id      BIGINT      NOT NULL,
    passed              BOOLEAN     NOT NULL,
    actual_output_json  MEDIUMTEXT  NULL,
    simulation_time_ns  BIGINT      NULL,
    wall_time_ms        INT         NULL,
    stderr_tail         MEDIUMTEXT  NULL,
    PRIMARY KEY (id),
    KEY idx_simulation_runs_submission (submission_id),
    KEY idx_simulation_runs_test_vector (test_vector_id),
    CONSTRAINT fk_simulation_runs_submission FOREIGN KEY (submission_id) REFERENCES submissions (id) ON DELETE CASCADE,
    CONSTRAINT fk_simulation_runs_test_vector FOREIGN KEY (test_vector_id) REFERENCES test_vectors (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE synthesis_results (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code_hash   CHAR(64)     NOT NULL,
    svg_path    VARCHAR(512) NOT NULL,
    gate_count  INT          NOT NULL,
    ff_count    INT          NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_synthesis_results_code_hash (code_hash)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE leaderboard_entries (
    id                    BIGINT      NOT NULL AUTO_INCREMENT,
    challenge_id          BIGINT      NOT NULL,
    user_id               BIGINT      NOT NULL,
    best_score            INT         NOT NULL DEFAULT 0,
    best_gate_count       INT         NULL,
    best_submission_id    BIGINT      NULL,
    updated_at            DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_leaderboard_challenge_user (challenge_id, user_id),
    KEY idx_leaderboard_challenge_score (challenge_id, best_score),
    CONSTRAINT fk_leaderboard_challenge FOREIGN KEY (challenge_id) REFERENCES challenges (id) ON DELETE CASCADE,
    CONSTRAINT fk_leaderboard_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_leaderboard_best_submission FOREIGN KEY (best_submission_id) REFERENCES submissions (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
