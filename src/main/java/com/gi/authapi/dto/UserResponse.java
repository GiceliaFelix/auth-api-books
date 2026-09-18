package com.gi.authapi.dto;

import com.gi.authapi.model.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        String role
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
