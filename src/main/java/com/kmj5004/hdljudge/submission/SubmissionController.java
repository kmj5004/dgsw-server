package com.kmj5004.hdljudge.submission;

import com.kmj5004.hdljudge.common.web.ApiResponse;
import com.kmj5004.hdljudge.common.web.PageResponse;
import com.kmj5004.hdljudge.security.AuthPrincipal;
import com.kmj5004.hdljudge.submission.dto.SubmissionAccepted;
import com.kmj5004.hdljudge.submission.dto.SubmissionDetail;
import com.kmj5004.hdljudge.submission.dto.SubmissionListItem;
import com.kmj5004.hdljudge.submission.dto.SubmissionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubmissionAccepted> submit(
        @AuthenticationPrincipal AuthPrincipal principal,
        @Valid @RequestBody SubmissionRequest req
    ) {
        return ApiResponse.ok(submissionService.submit(principal, req));
    }

    @GetMapping("/{id}")
    public ApiResponse<SubmissionDetail> get(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable Long id
    ) {
        return ApiResponse.ok(submissionService.get(principal, id));
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<SubmissionListItem>> listMine(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam(required = false) Long challengeId,
        @PageableDefault(size = 20, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.ok(submissionService.listMine(principal, challengeId, pageable));
    }
}
