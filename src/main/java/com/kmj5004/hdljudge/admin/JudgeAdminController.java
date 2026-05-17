package com.kmj5004.hdljudge.admin;

import com.kmj5004.hdljudge.common.web.ApiResponse;
import com.kmj5004.hdljudge.judge.JudgeOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;





@RestController
@RequestMapping("/api/admin/judge")
@RequiredArgsConstructor
public class JudgeAdminController {

    private final JudgeOrchestrator orchestrator;

    @GetMapping("/stats")
    public ApiResponse<JudgeOrchestrator.Stats> stats() {
        return ApiResponse.ok(orchestrator.stats());
    }
}
