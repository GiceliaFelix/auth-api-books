package com.gi.authapi.service;

import com.gi.authapi.dto.AuthResponse;
import com.gi.authapi.dto.LoginRequest;
import com.gi.authapi.dto.RegisterRequest;
import com.gi.authapi.exception.EmailAlreadyExistsException;
import com.gi.authapi.model.Role;
import com.gi.authapi.model.User;
import com.gi.authapi.repository.UserRepository;
import com.gi.authapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("Gi Developer", "gi@example.com", "senha123");

        savedUser = User.builder()
                .id(1L)
                .name("Gi Developer")
                .email("gi@example.com")
                .password("senha-encriptada")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    void deveRegistrarNovoUsuarioComSucesso() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("senha-encriptada");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("gi@example.com", response.email());
        assertEquals("ROLE_USER", response.role());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoAoRegistrarEmailJaExistente() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveAutenticarUsuarioComCredenciaisValidas() {
        LoginRequest loginRequest = new LoginRequest("gi@example.com", "senha123");

        when(userRepository.findByEmail("gi@example.com")).thenReturn(Optional.of(savedUser));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        verify(authenticationManager, times(1)).authenticate(any());
    }
}
