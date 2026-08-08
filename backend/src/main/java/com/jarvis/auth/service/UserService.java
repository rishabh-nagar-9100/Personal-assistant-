package com.jarvis.auth.service;

import com.jarvis.auth.model.User;
import com.jarvis.auth.repository.UserRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Finds the existing user or creates a new one from JWT claims.
     * Called on every authenticated request to keep user data in sync.
     */
    @org.springframework.transaction.annotation.Transactional
    public User getOrCreateUser(JwtAuthenticationToken authToken) {
        String sub = authToken.getToken().getSubject();
        UUID userId = UUID.fromString(sub);
        String email = authToken.getToken().getClaimAsString("email");

        return userRepository.findById(userId)
                .map(existing -> {
                    // Update email if it changed in Supabase
                    if (email != null && !email.equals(existing.getEmail())) {
                        existing.setEmail(email);
                        return userRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    User newUser = new User(userId, email);
                    return userRepository.save(newUser);
                });
    }
}
