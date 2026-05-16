package com.kmj5004.hdljudge.security;

import com.kmj5004.hdljudge.common.enums.Role;

public record AuthPrincipal(Long userId, String email, Role role) {
}
