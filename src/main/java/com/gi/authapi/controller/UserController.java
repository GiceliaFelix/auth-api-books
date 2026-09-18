package com.gi.authapi.controller;

import com.gi.authapi.dto.UserResponse;
import com.gi.authapi.model.User;
import com.gi.authapi.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/users/me")
    @Tag(name = "Usuário", description = "Dados do usuário autenticado")
    @Operation(summary = "Retorna os dados do usuário autenticado a partir do token JWT")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @GetMapping("/admin/users")
    @Tag(name = "Admin", description = "Endpoints restritos a administradores (ROLE_ADMIN)")
    @Operation(summary = "Lista todos os usuários cadastrados — acesso restrito a admins")
    public ResponseEntity<List<UserResponse>> listAll() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
