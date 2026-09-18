package com.gi.authapi.service;

import com.gi.authapi.model.Role;
import com.gi.authapi.model.User;
import com.gi.authapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    private static final String TEST_SECRET = "test-secret-key-for-unit-tests-only-do-not-use-in-production-1234567890";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L);

        user = User.builder()
                .id(1L)
                .name("Gi Developer")
                .email("gi@example.com")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    void deveGerarAccessTokenValido() {
        String token = jwtService.generateAccessToken(user);

        assertNotNull(token);
        assertEquals("gi@example.com", jwtService.extractUsername(token));
        assertFalse(jwtService.isRefreshToken(token));
    }

    @Test
    void deveGerarRefreshTokenValido() {
        String token = jwtService.generateRefreshToken(user);

        assertNotNull(token);
        assertTrue(jwtService.isRefreshToken(token));
    }

    @Test
    void deveValidarTokenDoUsuarioCorreto() {
        String token = jwtService.generateAccessToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void naoDeveValidarTokenParaUsuarioDiferente() {
        String token = jwtService.generateAccessToken(user);

        User outroUsuario = User.builder()
                .email("outro@example.com")
                .password("x")
                .role(Role.ROLE_USER)
                .build();

        assertFalse(jwtService.isTokenValid(token, outroUsuario));
    }

    @Test
    void deveLancarExcecaoParaTokenMalformado() {
        assertThrows(Exception.class, () -> jwtService.extractUsername("token-invalido"));
    }
}
