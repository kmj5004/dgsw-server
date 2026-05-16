package com.kmj5004.hdljudge.auth.dto;

import com.kmj5004.hdljudge.common.enums.Role;
import com.kmj5004.hdljudge.domain.user.User;

public record UserSummary(Long id, String email, Role role) {

    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getEmail(), user.getRole());
    }
}
