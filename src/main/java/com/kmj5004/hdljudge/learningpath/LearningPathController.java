package com.kmj5004.hdljudge.learningpath;

import com.kmj5004.hdljudge.common.web.ApiResponse;
import com.kmj5004.hdljudge.learningpath.dto.LearningPathDetail;
import com.kmj5004.hdljudge.learningpath.dto.LearningPathSummary;
import com.kmj5004.hdljudge.security.AuthPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;






@RestController
@RequestMapping("/api/paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningPathService service;

    @GetMapping
    public ApiResponse<List<LearningPathSummary>> list() {
        return ApiResponse.ok(service.list());
    }

    @GetMapping("/{slug}")
    public ApiResponse<LearningPathDetail> get(
        @PathVariable String slug,
        @AuthenticationPrincipal AuthPrincipal principal
    ) {
        Long userId = principal == null ? null : principal.userId();
        return ApiResponse.ok(service.getBySlug(slug, userId));
    }
}
