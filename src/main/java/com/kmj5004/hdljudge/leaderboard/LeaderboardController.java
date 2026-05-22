package com.kmj5004.hdljudge.leaderboard;

import com.kmj5004.hdljudge.common.web.ApiResponse;
import com.kmj5004.hdljudge.leaderboard.dto.LeaderboardEntryView;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/{slug}/leaderboard")
    public ApiResponse<List<LeaderboardEntryView>> leaderboard(@PathVariable String slug) {
        return ApiResponse.ok(leaderboardService.topBySlug(slug));
    }
}
