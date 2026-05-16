package com.kmj5004.hdljudge.auth;

import com.kmj5004.hdljudge.auth.dto.LoginRequest;
import com.kmj5004.hdljudge.auth.dto.RefreshRequest;
import com.kmj5004.hdljudge.auth.dto.SignupRequest;
import com.kmj5004.hdljudge.auth.dto.TokenResponse;
import com.kmj5004.hdljudge.auth.dto.UserSummary;
import com.kmj5004.hdljudge.auth.service.AuthService;
import com.kmj5004.hdljudge.common.web.ApiResponse;
import com.kmj5004.hdljudge.security.AuthPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserSummary> signup(@Valid @RequestBody SignupRequest req) {
        return ApiResponse.ok(authService.signup(req));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ApiResponse.ok(authService.refresh(req.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal AuthPrincipal principal) {
        authService.logout(principal.userId());
    }
}
