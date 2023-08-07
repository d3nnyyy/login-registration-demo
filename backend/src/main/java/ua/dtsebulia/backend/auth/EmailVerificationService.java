package ua.dtsebulia.backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ua.dtsebulia.backend.auth.token.VerificationToken;
import ua.dtsebulia.backend.auth.token.VerificationTokenRepository;
import ua.dtsebulia.backend.auth.token.VerificationTokenService;
import ua.dtsebulia.backend.user.User;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final AuthenticationService authenticationService;
    private final VerificationTokenService verificationTokenService;
    private final HttpServletRequest servletRequest;

    public ResponseEntity<?> verifyEmail(String token) {
        try {
            VerificationToken theToken = tokenRepository.findByToken(token);

            if (theToken == null) {
                return ResponseEntity.badRequest().body("Invalid verification token");
            }

            if (theToken.getUser().isEnabled()) {
                return ResponseEntity.badRequest().body("This account has already been verified, please login.");
            }

            String verificationResult = verificationTokenService.validateToken(token);

            if (verificationResult.equalsIgnoreCase("valid")) {
                User user = theToken.getUser();
                user.setEnabled(true);
                authenticationService.saveUser(user);
                return ResponseEntity.ok("Email verified successfully. Now you can login to your account.");
            }

            return ResponseEntity.badRequest().body("Invalid verification token. <a href=\"" + getResendVerificationLink(token) + "\">Get a new verification link</a>");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    private String getResendVerificationLink(String token) {
        return authenticationService.applicationUrl(servletRequest) + "/api/v1/auth/resend-verification-token?token=" + token;
    }

}
