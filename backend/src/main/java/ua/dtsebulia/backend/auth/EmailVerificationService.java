package ua.dtsebulia.backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.dtsebulia.backend.auth.token.VerificationToken;
import ua.dtsebulia.backend.auth.token.VerificationTokenRepository;
import ua.dtsebulia.backend.auth.token.VerificationTokenService;
import ua.dtsebulia.backend.exception.InvalidTokenException;
import ua.dtsebulia.backend.exception.VerificationException;
import ua.dtsebulia.backend.user.User;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final AuthenticationService authenticationService;
    private final VerificationTokenService verificationTokenService;
    private final HttpServletRequest servletRequest;

    public String verifyEmail(String token) {
        VerificationToken theToken = tokenRepository.findByToken(token);

        if (theToken == null) {
            throw new InvalidTokenException("Invalid verification token");
        }

        if (theToken.getUser().isEnabled()) {
            throw new VerificationException("This account has already been verified, please login.");
        }

        String verificationResult = verificationTokenService.validateToken(token);

        if (verificationResult.equalsIgnoreCase("valid")) {
            User user = theToken.getUser();
            user.setEnabled(true);
            authenticationService.saveUser(user);
            return "Email verified successfully. Now you can login to your account.";
        }

        return "Invalid verification token. Get a new verification link: " + getResendVerificationLink(token);
    }

    private String getResendVerificationLink(String token) {
        return authenticationService.applicationUrl(servletRequest) + "/api/v1/auth/resend-verification-token?token=" + token;
    }

}
