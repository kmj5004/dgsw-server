package com.kmj5004.hdljudge.me;

import com.kmj5004.hdljudge.common.web.ApiResponse;
import com.kmj5004.hdljudge.me.dto.UserProgress;
import com.kmj5004.hdljudge.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final MeService meService;

    @GetMapping("/progress")
    public ApiResponse<UserProgress> progress(@AuthenticationPrincipal AuthPrincipal principal) {
        return ApiResponse.ok(meService.getProgress(principal.userId()));
    }
}
