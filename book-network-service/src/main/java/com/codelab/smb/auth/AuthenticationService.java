package com.codelab.smb.auth;

import com.codelab.smb.email.EmailService;
import com.codelab.smb.email.EmailTemplate;
import com.codelab.smb.role.RoleRepository;
import com.codelab.smb.user.Token;
import com.codelab.smb.user.TokenRepository;
import com.codelab.smb.user.User;
import com.codelab.smb.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final TokenRepository tokenRepository;

    private final EmailService emailService;

    @Value("${application.security.mailing.frontend.activationUrl}")
    private String activationUrl;

    public void registerUser(RegistrationRequest regRequest) throws MessagingException {
        // Set default role 'USER'
        var userRole = roleRepository.findByName("USER")
                // implement a custom exception later
                .orElseThrow(() -> new IllegalStateException(("Role USER was not initialized")));

        // Saving the User
        var user = User.builder()
                .firstName(regRequest.getFirstName())
                .lastName(regRequest.getLastName())
                .email(regRequest.getEmail())
                .password(passwordEncoder.encode(regRequest.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();
        userRepository.save(user);

        // Email validation
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);

        // send Email
        emailService.sendEmail(user.getEmail(), user.getFullName(),
                EmailTemplate.ACTIVATE_ACCOUNT, activationUrl,
                newToken, "Account activation");
    }

    private String generateAndSaveActivationToken(User user) {
        // Generate token
        String generatedToken = generateActivationToken();

        // Save Token
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        tokenRepository.save(token);

        return generatedToken;
    }

    private String generateActivationToken() {
        int length = 6;
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }
}
