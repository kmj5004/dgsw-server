package com.kmj5004.hdljudge.me.dto;

import com.kmj5004.hdljudge.learningpath.dto.PathProgress;
import com.kmj5004.hdljudge.submission.dto.SubmissionListItem;
import java.util.List;

public record UserProgress(
    long totalSubmissions,
    long attemptedChallenges,
    long solvedChallenges,
    List<SubmissionListItem> recentSubmissions,
    List<PathProgress> paths
) {
}
