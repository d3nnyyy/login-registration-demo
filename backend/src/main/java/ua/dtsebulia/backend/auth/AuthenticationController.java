package ua.dtsebulia.backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.dtsebulia.backend.auth.password.PasswordResetRequest;
import ua.dtsebulia.backend.auth.password.PasswordResetService;
import ua.dtsebulia.backend.auth.token.ResendVerificationTokenService;

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
        return ResponseEntity.ok(authenticationService.register(registrationRequest, request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest));
    }

    @GetMapping("/verifyEmail")
    public ResponseEntity<?> verifyEmail(
            @RequestParam("token") String token) {
        return ResponseEntity.ok(emailVerificationService.verifyEmail(token));
    }

    @GetMapping("/resend-verification-token")
    public ResponseEntity<?> resendVerificationToken(
            @RequestParam("token") String oldToken,
            HttpServletRequest request) {
        return ResponseEntity.ok(resendVerificationTokenService.resendVerificationToken(oldToken, request));
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<?> resetPasswordRequest(
            @RequestBody PasswordResetRequest passwordResetRequest,
            HttpServletRequest request) {
        return ResponseEntity.ok(passwordResetService.resetPasswordRequest(passwordResetRequest, request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody PasswordResetRequest passwordResetRequest,
            @RequestParam("token") String passwordResetToken) {
        return ResponseEntity.ok(passwordResetService.resetPassword(passwordResetRequest, passwordResetToken));
    }

}
