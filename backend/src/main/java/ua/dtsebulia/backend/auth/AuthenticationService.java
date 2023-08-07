package ua.dtsebulia.backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.dtsebulia.backend.auth.token.VerificationToken;
import ua.dtsebulia.backend.auth.token.VerificationTokenRepository;
import ua.dtsebulia.backend.config.JwtService;
import ua.dtsebulia.backend.event.RegistrationCompleteEvent;
import ua.dtsebulia.backend.exception.UserAlreadyExistsException;
import ua.dtsebulia.backend.user.User;
import ua.dtsebulia.backend.user.UserRepository;

import java.net.URI;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final JwtService jwtService;
    private final ApplicationEventPublisher publisher;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<?> register(RegistrationRequest registrationRequest, HttpServletRequest request) {
        Optional<User> user = this.findByEmail(registrationRequest.getEmail());

        if (user.isPresent()) {
            throw new UserAlreadyExistsException(
                    "User with email " + registrationRequest.getEmail() + " already exists");
        }

        User newUser = User.builder()
                .firstName(registrationRequest.getFirstname())
                .lastName(registrationRequest.getLastname())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .role(registrationRequest.getRole())
                .build();
        userRepository.save(newUser);

        publisher.publishEvent(new RegistrationCompleteEvent(newUser, applicationUrl(request)));

        return ResponseEntity.ok("Registration successful. Please check your email for verification link");

    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        ));

        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public void saveUserVerificationToken(User theUser, String token) {
        var verificationToken = new VerificationToken(token, theUser);
        tokenRepository.save(verificationToken);
    }

    public void changeUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
