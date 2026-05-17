package com.kmj5004.hdljudge.challenge;

import com.kmj5004.hdljudge.challenge.dto.ChallengeCreateRequest;
import com.kmj5004.hdljudge.challenge.dto.ChallengeDetail;
import com.kmj5004.hdljudge.challenge.dto.ChallengeSummary;
import com.kmj5004.hdljudge.challenge.dto.ChallengeUpdateRequest;
import com.kmj5004.hdljudge.challenge.service.ChallengeAdminService;
import com.kmj5004.hdljudge.challenge.service.ChallengeQueryService;
import com.kmj5004.hdljudge.common.enums.Difficulty;
import com.kmj5004.hdljudge.common.web.ApiResponse;
import com.kmj5004.hdljudge.common.web.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeQueryService queryService;
    private final ChallengeAdminService adminService;

    @GetMapping
    public ApiResponse<PageResponse<ChallengeSummary>> list(
        @RequestParam(required = false) Difficulty difficulty,
        @RequestParam(required = false) String tag,
        @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.ok(queryService.list(difficulty, tag, pageable));
    }

    @GetMapping("/{slug}")
    public ApiResponse<ChallengeDetail> getBySlug(@PathVariable String slug) {
        return ApiResponse.ok(queryService.getBySlug(slug));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ChallengeDetail> create(@Valid @RequestBody ChallengeCreateRequest req) {
        return ApiResponse.ok(adminService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<ChallengeDetail> update(
        @PathVariable Long id,
        @Valid @RequestBody ChallengeUpdateRequest req
    ) {
        return ApiResponse.ok(adminService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        adminService.softDelete(id);
    }
}
