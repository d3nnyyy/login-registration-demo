package ua.dtsebulia.backend.auth;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.dtsebulia.backend.auth.password.PasswordResetRequest;
import ua.dtsebulia.backend.auth.token.VerificationToken;
import ua.dtsebulia.backend.auth.token.VerificationTokenRepository;
import ua.dtsebulia.backend.config.JwtService;
import ua.dtsebulia.backend.event.RegistrationCompleteEvent;
import ua.dtsebulia.backend.event.RegistrationCompleteEventListener;
import ua.dtsebulia.backend.exception.UserAlreadyExistsException;
import ua.dtsebulia.backend.user.User;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService userService;
    private final ApplicationEventPublisher publisher;
    private final VerificationTokenRepository tokenRepository;
    private final RegistrationCompleteEventListener eventListener;
    private final HttpServletRequest servletRequest;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestBody RegistrationRequest registrationRequest,
            final HttpServletRequest request) {
        try {
            User user = userService.register(registrationRequest);
            publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));
            return ResponseEntity.ok("Success! Please check your email to complete your registration");
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.badRequest().body("User with this email already exists");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(userService.authenticate(authenticationRequest));
    }

    @GetMapping("/verifyEmail")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        try {
            VerificationToken theToken = tokenRepository.findByToken(token);

            if (theToken == null) {
                return ResponseEntity.badRequest().body("Invalid verification token");
            }

            if (theToken.getUser().isEnabled()) {
                return ResponseEntity.badRequest().body("This account has already been verified, please login.");
            }

            String verificationResult = userService.validateToken(token);

            if (verificationResult.equalsIgnoreCase("valid")) {
                User user = theToken.getUser();
                user.setEnabled(true);
                userService.saveUser(user);
                return ResponseEntity.ok("Email verified successfully. Now you can login to your account.");
            }

            return ResponseEntity.badRequest().body("Invalid verification token. <a href=\"" + getResendVerificationLink(token) + "\">Get a new verification link</a>");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    private String getResendVerificationLink(String token) {
        return applicationUrl(servletRequest) + "/api/v1/auth/resend-verification-token?token=" + token;
    }


    @GetMapping("/resend-verification-token")
    public ResponseEntity<?> resendVerificationToken(@RequestParam("token") String oldToken, final HttpServletRequest request) {
        try {
            VerificationToken verificationToken = userService.generateNewVerificationToken(oldToken);
            User user = verificationToken.getUser();
            resendRegistrationToken(user, applicationUrl(request), verificationToken);
            return ResponseEntity.ok("A new verification email has been sent to your email address");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    private void resendRegistrationToken(User user, String applicationUrl, VerificationToken verificationToken) throws MessagingException, UnsupportedEncodingException {
        String url = applicationUrl + "/api/v1/auth/verifyEmail?token=" + verificationToken.getToken();
        eventListener.sendVerificationEmail(url);
        log.info("Click the link to verify your email :  {}", url);
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<?> resetPasswordRequest(@RequestBody PasswordResetRequest passwordResetRequest, HttpServletRequest request) {
        try {
            Optional<User> user = userService.findByEmail(passwordResetRequest.getEmail());

            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body("No user found with this email address");
            }

            String passwordResetToken = jwtService.generateToken(new HashMap<>(), user.get());
            userService.createPasswordResetTokenForUser(user.get(), passwordResetToken);

            return ResponseEntity.ok(passwordResetEmailLink(user.get(), applicationUrl(request), passwordResetToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    private String passwordResetEmailLink(User user, String applicationUrl, String passwordResetToken) throws MessagingException, UnsupportedEncodingException {
        String url = applicationUrl + "/api/v1/auth/reset-password?token=" + passwordResetToken;
        eventListener.sendPasswordResetVerificationEmail(url);
        log.info("Click the link to reset your password :  {}", url);
        return url;
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest passwordResetRequest, @RequestParam("token") String passwordResetToken) {
        try {
            String tokenValidationResult = userService.validatePasswordResetToken(passwordResetToken);
            if (tokenValidationResult.equalsIgnoreCase("valid")) {
                User user = userService.findUserByPasswordResetToken(passwordResetToken);
                if (user != null) {
                    userService.changeUserPassword(user, passwordResetRequest.getNewPassword());
                    return ResponseEntity.ok("Password reset successfully.");
                }
            }
            return ResponseEntity.badRequest().body("Invalid password reset token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    public String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

}
