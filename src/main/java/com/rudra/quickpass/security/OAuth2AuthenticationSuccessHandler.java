package com.rudra.quickpass.security;

import com.rudra.quickpass.entity.AuthProvider;
import com.rudra.quickpass.entity.Role;
import com.rudra.quickpass.entity.User;
import com.rudra.quickpass.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Value("${quickpass.jwt.cookie-name:quickpass_token}")
    private String cookieName;

    public OAuth2AuthenticationSuccessHandler(UserRepository userRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String sub = oAuth2User.getAttribute("sub");

        if (email == null || email.isBlank()) {
            log.error("Google OAuth response missing email attribute!");
            response.sendRedirect("/login?error=Google authentication failed: Email not provided.");
            return;
        }

        if (name == null || name.isBlank()) {
            name = email.split("@")[0];
        }

        // Account Linking: Link existing account or create new OAuth user
        final String finalName = name;
        final String finalSub = sub;

        User user = userRepository.findByEmail(email).map(existingUser -> {
            boolean updated = false;
            // Scenario 1: Account Linking
            if (existingUser.getProviderId() == null && finalSub != null) {
                existingUser.setProviderId(finalSub);
                updated = true;
                log.info("Linked Google account (sub: {}) to existing user: {}", finalSub, email);
            }
            if (existingUser.getAuthProvider() == null) {
                existingUser.setAuthProvider(AuthProvider.GOOGLE);
                updated = true;
            }
            return updated ? userRepository.save(existingUser) : existingUser;
        }).orElseGet(() -> {
            // New Google Signup
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(finalName);
            newUser.setPassword(null); // No password for pure OAuth user
            newUser.setRole(Role.ROLE_ATTENDEE);
            newUser.setAuthProvider(AuthProvider.GOOGLE);
            newUser.setProviderId(finalSub);
            log.info("Created new user via Google OAuth: {}", email);
            return userRepository.save(newUser);
        });

        // Generate QuickPass JWT Token
        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), user.getFullName());

        // Set HttpOnly SameSite=Lax Cookie
        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Smart Role-Based Redirect
        String targetUrl = "/dashboard";
        if (user.getRole() == Role.ROLE_ADMIN) {
            targetUrl = "/admin";
        } else if (user.getRole() == Role.ROLE_STAFF) {
            targetUrl = "/staff/events";
        } else if (user.getRole() == Role.ROLE_ORGANIZER) {
            targetUrl = "/organizer";
        }

        response.sendRedirect(targetUrl);
    }
}
