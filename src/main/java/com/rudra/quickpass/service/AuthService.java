package com.rudra.quickpass.service;

import com.rudra.quickpass.dto.AuthResponse;
import com.rudra.quickpass.dto.LoginRequest;
import com.rudra.quickpass.dto.RegisterRequest;
import com.rudra.quickpass.entity.Role;
import com.rudra.quickpass.entity.User;
import com.rudra.quickpass.repository.UserRepository;
import com.rudra.quickpass.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public AuthResponse register(RegisterRequest request) {
        User existing = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (existing != null) {
            if (existing.getPassword() == null || existing.getPassword().isBlank()) {
                throw new IllegalArgumentException(
                    "An account with this email was created with Google Sign-In. Please click 'Continue with Google' to log in."
                );
            }
            throw new IllegalArgumentException("Email is already registered! Please log in.");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_ATTENDEE;

        User user = new User(
                request.getFullName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                role
        );

        userRepository.save(user);

        return new AuthResponse(
                null,
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                "User registered successfully! You can now log in."
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user != null && (user.getPassword() == null || user.getPassword().isBlank())) {
            throw new IllegalArgumentException(
                    "This account was registered using Google. Please log in by clicking 'Continue with Google'.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        if (user == null) {
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(
                            () -> new IllegalArgumentException("User not found with email: " + request.getEmail()));
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), user.getFullName());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                "Login successful!");
    }
}
