package com.springboot.api.service;


import com.springboot.api.entity.users.User;
import com.springboot.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.base-url}")
    private String baseUrl;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public String verifyAccount(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (user.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }
        user.setActive(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        userRepository.save(user);
        return "Account activated successfully";
    }

    public String resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isActive()) {
            return "Account already verified";
        }
        sendVerificationEmail(user);
        return "Verification email resent";
    }

    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        user.setActivationToken(token);
        user.setActivationTokenExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        String link = baseUrl + "/api/auth/verify?token=" + token;
        String htmlMessage = """
                 <html>
                 <body>
                     <h2>Verify Your Account</h2>
                     <p>Click the button below to activate your account:</p>
                     <a href="%s"
                        style="padding:10px 20px;background:#4CAF50;color:white;text-decoration:none;">
                        Activate Account
                     </a>
                     <p>This link expires in 5 minute.</p>
                 </body>
                 </html>
                """.formatted(link);
        emailService.sendHtmlEmail(user.getEmail(), "Account Verification", htmlMessage);
    }
}