package ua.dtsebulia.backend.auth.password;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ua.dtsebulia.backend.auth.AuthenticationService;
import ua.dtsebulia.backend.auth.password.PasswordResetRequest;
import ua.dtsebulia.backend.auth.password.PasswordResetTokenService;
import ua.dtsebulia.backend.config.JwtService;
import ua.dtsebulia.backend.event.RegistrationCompleteEventListener;
import ua.dtsebulia.backend.user.User;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final AuthenticationService authenticationService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final JwtService jwtService;
    private final RegistrationCompleteEventListener eventListener;

    public ResponseEntity<?> resetPasswordRequest(PasswordResetRequest passwordResetRequest, HttpServletRequest request) {
        try {
            Optional<User> user = authenticationService.findByEmail(passwordResetRequest.getEmail());

            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body("No user found with this email address");
            }

            String passwordResetToken = jwtService.generateToken(user.get());
            passwordResetTokenService.createPasswordResetTokenForUser(user.get(), passwordResetToken);

            return ResponseEntity.ok(passwordResetEmailLink(applicationUrl(request), passwordResetToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    private String passwordResetEmailLink(String applicationUrl, String passwordResetToken) throws MessagingException, UnsupportedEncodingException {
        String url = applicationUrl + "/api/v1/auth/reset-password?token=" + passwordResetToken;
        eventListener.sendPasswordResetVerificationEmail(url);
        log.info("Click the link to reset your password: {}", url);
        return url;
    }

    public ResponseEntity<?> resetPassword(PasswordResetRequest passwordResetRequest, String passwordResetToken) {
        try {
            String tokenValidationResult = passwordResetTokenService.validatePasswordResetToken(passwordResetToken);
            if (tokenValidationResult.equalsIgnoreCase("valid")) {
                User user = passwordResetTokenService.findUserByPasswordResetToken(passwordResetToken).orElse(null);
                if (user != null) {
                    authenticationService.changeUserPassword(user, passwordResetRequest.getNewPassword());
                    return ResponseEntity.ok("Password reset successfully.");
                }
            }
            return ResponseEntity.badRequest().body("Invalid password reset token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}

