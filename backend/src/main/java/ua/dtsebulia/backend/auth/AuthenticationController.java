package ua.dtsebulia.backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import ua.dtsebulia.backend.auth.password.PasswordResetRequest;
import ua.dtsebulia.backend.auth.password.PasswordResetService;
import ua.dtsebulia.backend.auth.token.ResendVerificationTokenService;
import ua.dtsebulia.backend.exception.InvalidTokenException;
import ua.dtsebulia.backend.exception.PasswordResetException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final EmailVerificationService emailVerificationService;
    private final ResendVerificationTokenService resendVerificationTokenService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegistrationRequest registrationRequest,
            HttpServletRequest request) {
        try {
            return ResponseEntity.ok(authenticationService.register(registrationRequest, request));
        } catch (Exception e) {
            log.error("Error during registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during registration.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @RequestBody AuthenticationRequest authenticationRequest) {
        try {
            return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest));
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed.");
        }
    }

    @GetMapping("/verifyEmail")
    public ResponseEntity<?> verifyEmail(
            @RequestParam("token") String token) {
        try {
            return ResponseEntity.ok(emailVerificationService.verifyEmail(token));
        } catch (Exception e) {
            log.error("Email verification error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email verification failed.");
        }
    }

    @GetMapping("/resend-verification-token")
    public ResponseEntity<?> resendVerificationToken(
            @RequestParam("token") String oldToken,
            HttpServletRequest request) {
        try {
            return ResponseEntity.ok(resendVerificationTokenService.resendVerificationToken(oldToken, request));
        } catch (Exception e) {
            log.error("Resend verification token error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while resending verification token.");
        }
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<String> resetPasswordRequest(
            @RequestBody PasswordResetRequest passwordResetRequest,
            HttpServletRequest request) {
        try {
            String result = passwordResetService.resetPasswordRequest(passwordResetRequest, request);
            return ResponseEntity.ok(result);
        } catch (UsernameNotFoundException e) {
            log.error("Username not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (Exception e) {
            log.error("Password reset request error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during password reset request.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody PasswordResetRequest passwordResetRequest,
            @RequestParam("token") String passwordResetToken) {
        try {
            String result = passwordResetService.resetPassword(passwordResetRequest, passwordResetToken);
            return ResponseEntity.ok(result);
        } catch (InvalidTokenException e) {
            log.error("Invalid password reset token: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid password reset token.");
        } catch (PasswordResetException e) {
            log.error("Password reset error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Password reset failed.");
        } catch (Exception e) {
            log.error("Unexpected error during password reset: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during password reset.");
        }
    }
}
