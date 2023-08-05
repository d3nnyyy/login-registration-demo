package ua.dtsebulia.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.dtsebulia.backend.auth.password.PasswordResetTokenService;
import ua.dtsebulia.backend.auth.token.VerificationToken;
import ua.dtsebulia.backend.auth.token.VerificationTokenRepository;
import ua.dtsebulia.backend.config.JwtService;
import ua.dtsebulia.backend.exception.UserAlreadyExistsException;
import ua.dtsebulia.backend.user.User;
import ua.dtsebulia.backend.user.UserRepository;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenService passwordResetTokenService;
    private final VerificationTokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public User register(RegistrationRequest request) {
        Optional<User> user = this.findByEmail(request.getEmail());
        if (user.isPresent()) {
            throw new UserAlreadyExistsException(
                    "User with email " + request.getEmail() + " already exists");
        }
        var newUser = new User();
        newUser.setFirstName(request.getFirstname());
        newUser.setLastName(request.getLastname());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(request.getRole());
        return userRepository.save(newUser);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void saveUserVerificationToken(User theUser, String token) {
        var verificationToken = new VerificationToken(token, theUser);
        tokenRepository.save(verificationToken);
    }

    public String validateToken(String theToken) {
        VerificationToken token = tokenRepository.findByToken(theToken);
        if (token == null) {
            return "Invalid verification token";
        }
        User user = token.getUser();
        Calendar calendar = Calendar.getInstance();
        if ((token.getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0) {
            tokenRepository.delete(token);
            return "Token already expired";
        }
        user.setEnabled(true);
        userRepository.save(user);
        return "valid";
    }

    public void createPasswordResetTokenForUser(User user, String passwordToken) {
        passwordResetTokenService.createPasswordResetTokenForUser(user, passwordToken);
    }

    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken token = tokenRepository.findByToken(oldToken);
        var verificationTokenTime = new VerificationToken();
        token.setToken(jwtService.generateToken(new HashMap<>(), token.getUser()));
        token.setExpirationTime(verificationTokenTime.getExpirationTime());
        return tokenRepository.save(token);
    }

    public String validatePasswordResetToken(String passwordResetToken) {
        return passwordResetTokenService.validatePasswordResetToken(passwordResetToken);
    }

    public User findUserByPasswordResetToken(String passwordResetToken) {
        return passwordResetTokenService.findUserByPasswordResetToken(passwordResetToken).get();
    }

    public void changeUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
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

    public void saveUser(User user) {
        userRepository.save(user);
    }

}
