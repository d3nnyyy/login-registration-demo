package ua.dtsebulia.backend.event;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import ua.dtsebulia.backend.config.JwtService;
import ua.dtsebulia.backend.user.User;
import ua.dtsebulia.backend.user.UserService;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    private final UserService userService;
    private final JavaMailSender mailSender;
    private final JwtService jwtService;

    private User user;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        user = event.getUser();
        String verificationToken = jwtService.generateToken(new HashMap<>(), user);
        userService.saveUserVerificationToken(user, verificationToken);
        String url = event.getApplicationUrl() + "/api/v1/auth/verifyEmail?token=" + verificationToken;
        try {
            sendVerificationEmail(url);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        log.info("Click the link to verify your registration :  {}", url);
    }

    public void sendVerificationEmail(String url) throws MessagingException, UnsupportedEncodingException {
        String subject = "Email Verification";
        String senderName = "User Registration Portal Service";
        String mailContent = "<p> Hi, " + user.getFirstName() + ", </p>" +
                "<p>Thank you for registering with us," +
                "Please, follow the link below to complete your registration.</p>" +
                "<a href=\"" + url + "\">Verify your email to activate your account</a>" +
                "<p> Thank you <br> Users Registration Portal Service";
        sendEmail(subject, senderName, user.getEmail(), mailContent);
    }

    public void sendPasswordResetVerificationEmail(String url) throws MessagingException, UnsupportedEncodingException {
        String subject = "Password Reset Request Verification";
        String senderName = "User Registration Portal Service";
        String mailContent = "<p> Hi, " + user.getFirstName() + ", </p>" +
                "<p>You have recently requested to reset your password," +
                "Please, follow the link below to complete this action.</p>" +
                "<a href=\"" + url + "\">Reset Password</a>" +
                "<p>Users Registration Portal Service";
        sendEmail(subject, senderName, user.getEmail(), mailContent);
    }

    private void sendEmail(String subject, String senderName, String recipientEmail, String mailContent) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message);
        messageHelper.setFrom("d3nnn41k@gmail.com", senderName);
        messageHelper.setTo(recipientEmail);
        messageHelper.setSubject(subject);
        messageHelper.setText(mailContent, true);
        mailSender.send(message);
    }

}
