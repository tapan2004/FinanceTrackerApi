package com.springboot.api.controller;


import com.springboot.api.dto.request.LoginRequest;
import com.springboot.api.dto.request.SignupRequest;
import com.springboot.api.dto.response.UserResponse;
import com.springboot.api.entity.users.User;
import com.springboot.api.service.CloudinaryService;
import com.springboot.api.service.UserService;
import com.springboot.api.util.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final CloudinaryService cloudinaryService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
        }
        UserResponse savedUser = userService.registerUser(request);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(request.getEmail());

            String jwt = jwtUtils.generateToken(userDetails);
            return ResponseEntity.ok(jwt);
        } catch (Exception e) {
            log.error("Login error {}", e.getMessage());
            return new ResponseEntity<>("Invalid Username or Password", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserResponse profile = userService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/upload-profile")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws Exception {
        String email = authentication.getName();
        String imageUrl = cloudinaryService.uploadImage(file);
        User user = userService.findByEmail(email);
        user.setProfileImageUrl(imageUrl);
        userService.save(user);
        return ResponseEntity.ok(imageUrl);
    }

    @PutMapping("/edit/{email}")
    @PreAuthorize("#email == authentication.name or hasRole('ADMIN')")
    public ResponseEntity<?> edit(@PathVariable String email,
                                  @RequestBody SignupRequest request) {
        userService.updateUser(email, request);
        return ResponseEntity.ok("User updated successfully");
    }
}