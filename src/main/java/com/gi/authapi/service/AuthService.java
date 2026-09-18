package com.gi.authapi.service;

import com.gi.authapi.dto.AuthResponse;
import com.gi.authapi.dto.LoginRequest;
import com.gi.authapi.dto.RefreshRequest;
import com.gi.authapi.dto.RegisterRequest;
import com.gi.authapi.exception.EmailAlreadyExistsException;
import com.gi.authapi.exception.InvalidTokenException;
import com.gi.authapi.model.Role;
import com.gi.authapi.model.User;
import com.gi.authapi.repository.UserRepository;
import com.gi.authapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidTokenException("Usuário não encontrado"));

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();
        String userEmail = jwtService.extractUsername(token);

        if (userEmail == null || !jwtService.isRefreshToken(token)) {
            throw new InvalidTokenException("Refresh token inválido");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        if (!jwtService.isTokenValid(token, userDetails)) {
            throw new InvalidTokenException("Refresh token expirado ou inválido");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new InvalidTokenException("Usuário não encontrado"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
