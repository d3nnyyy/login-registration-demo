package ua.dtsebulia.backend.auth.token;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ua.dtsebulia.backend.auth.AuthenticationService;
import ua.dtsebulia.backend.event.RegistrationCompleteEventListener;

import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@Service
@Slf4j
public class ResendVerificationTokenService {

    private final VerificationTokenService verificationTokenService;
    private final RegistrationCompleteEventListener eventListener;
    private final AuthenticationService authenticationService;

    public ResponseEntity<?> resendVerificationToken(String oldToken, HttpServletRequest request) {
        try {
            VerificationToken verificationToken = verificationTokenService.generateNewVerificationToken(oldToken);
            resendRegistrationToken(authenticationService.applicationUrl(request), verificationToken);
            return ResponseEntity.ok("A new verification email has been sent to your email address");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    private void resendRegistrationToken(String applicationUrl, VerificationToken verificationToken) throws MessagingException, UnsupportedEncodingException {
        String url = applicationUrl + "/api/v1/auth/verifyEmail?token=" + verificationToken.getToken();
        eventListener.sendVerificationEmail(url);
        log.info("Click the link to verify your email: {}", url);
    }

}
