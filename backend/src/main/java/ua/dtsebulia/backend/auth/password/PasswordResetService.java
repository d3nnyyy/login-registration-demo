package ua.dtsebulia.backend.auth.password;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.dtsebulia.backend.auth.AuthenticationService;
import ua.dtsebulia.backend.auth.password.PasswordResetRequest;
import ua.dtsebulia.backend.auth.password.PasswordResetTokenService;
import ua.dtsebulia.backend.config.JwtService;
import ua.dtsebulia.backend.event.RegistrationCompleteEventListener;
import ua.dtsebulia.backend.exception.InvalidTokenException;
import ua.dtsebulia.backend.exception.PasswordResetException;
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


    public String resetPasswordRequest(PasswordResetRequest passwordResetRequest, HttpServletRequest request) throws UsernameNotFoundException, MessagingException, UnsupportedEncodingException {
        Optional<User> user = authenticationService.findByEmail(passwordResetRequest.getEmail());

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("No user found with email " + passwordResetRequest.getEmail() + ".");
        }

        String passwordResetToken = jwtService.generateToken(user.get());
        passwordResetTokenService.createPasswordResetTokenForUser(user.get(), passwordResetToken);

        return passwordResetEmailLink(applicationUrl(request), passwordResetToken);
    }

    public String resetPassword(PasswordResetRequest passwordResetRequest, String passwordResetToken) throws InvalidTokenException, PasswordResetException {
        String tokenValidationResult = passwordResetTokenService.validatePasswordResetToken(passwordResetToken);
        if (tokenValidationResult.equalsIgnoreCase("valid")) {
            User user = passwordResetTokenService.findUserByPasswordResetToken(passwordResetToken).orElse(null);
            if (user != null) {
                authenticationService.changeUserPassword(user, passwordResetRequest.getNewPassword());
                return "Password reset successfully.";
            }
        }
        throw new InvalidTokenException("Invalid password reset token");
    }


    private String passwordResetEmailLink(String applicationUrl, String passwordResetToken) throws MessagingException, UnsupportedEncodingException {
        String url = applicationUrl + "/api/v1/auth/reset-password?token=" + passwordResetToken;
        eventListener.sendPasswordResetVerificationEmail(url);
        log.info("Click the link to reset your password: {}", url);
        return url;
    }
    private String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}

