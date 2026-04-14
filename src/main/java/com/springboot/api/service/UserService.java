package com.springboot.api.service;

import com.springboot.api.dto.request.SignupRequest;
import com.springboot.api.dto.response.UserResponse;
import com.springboot.api.entity.roles.Role;
import com.springboot.api.entity.roles.RoleName;
import com.springboot.api.entity.users.User;
import com.springboot.api.repository.RoleRepository;
import com.springboot.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthService authService;

    @Transactional
    public UserResponse registerUser(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        Role role = roleRepository.findByUserRole(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(false)
                .roles(Set.of(role))
                .build();
        User savedUser = userRepository.save(user);

        // Send verification email (token handled inside AuthService)
        authService.sendVerificationEmail(savedUser);
        return mapToResponse(savedUser);
    }

    //ENTITY → DTO
    private UserResponse mapToResponse(User user) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(r -> r.getUserRole().name())
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roles)
                .build();
    }

    //  UPDATE
    @Transactional
    public void updateUser(String email, SignupRequest request) {

        User existing = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            existing.setUsername(request.getUsername());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getEmail() != null &&
                !request.getEmail().equals(existing.getEmail()) &&
                userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already taken");
        }
        if (request.getEmail() != null) {
            existing.setEmail(request.getEmail());
        }

        // Role update
        if (request.getRole() != null && !request.getRole().isBlank()) {
            Role newRole = "ROLE_ADMIN".equalsIgnoreCase(request.getRole())
                    ? roleRepository.findByUserRole(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"))
                    : roleRepository.findByUserRole(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
            existing.getRoles().clear();
            existing.getRoles().add(newRole);
        }
        userRepository.save(existing);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public UserResponse getUserProfile(String email) {
        User user = findByEmail(email);
        return mapToResponse(user);
    }
}