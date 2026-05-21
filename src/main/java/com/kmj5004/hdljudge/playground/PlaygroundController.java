package com.kmj5004.hdljudge.playground;

import com.kmj5004.hdljudge.common.web.ApiResponse;
import com.kmj5004.hdljudge.playground.dto.SimulateRequest;
import com.kmj5004.hdljudge.playground.dto.SimulateResponse;
import com.kmj5004.hdljudge.playground.dto.SynthesizeRequest;
import com.kmj5004.hdljudge.playground.dto.SynthesizeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playground")
@RequiredArgsConstructor
public class PlaygroundController {

    private final PlaygroundService playgroundService;

    @PostMapping("/simulate")
    public ApiResponse<SimulateResponse> simulate(@Valid @RequestBody SimulateRequest req) {
        return ApiResponse.ok(playgroundService.simulate(req));
    }

    @PostMapping("/synthesize")
    public ApiResponse<SynthesizeResponse> synthesize(@Valid @RequestBody SynthesizeRequest req) {
        return ApiResponse.ok(playgroundService.synthesize(req));
    }
}
