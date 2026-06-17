package com.springboot.api.service;

import com.springboot.api.entity.users.User;
import com.springboot.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.base-url}")
    private String baseUrl;
    
    @Value("${app.frontend-url:http://localhost}")
    private String frontendUrl;
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

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

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email address. Please sign up first."));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        String link = frontendUrl + "/reset-password?token=" + token;
        String htmlMessage = """
                <html>
                <body>
                    <h2>Reset Your Password</h2>
                    <p>Click the link below to reset your password:</p>
                    <a href="%s" style="padding:10px 20px;background:#2196F3;color:white;text-decoration:none;">
                        Reset Password
                    </a>
                    <p>This link expires in 15 minutes.</p>
                    <p>If you didn't request this, please ignore this email.</p>
                </body>
                </html>
                """.formatted(link);

        emailService.sendHtmlEmail(user.getEmail(), "Password Reset Request", htmlMessage);
        return "Password reset link sent to your email";
    }

    public String resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "Password has been successfully reset";
    }
}