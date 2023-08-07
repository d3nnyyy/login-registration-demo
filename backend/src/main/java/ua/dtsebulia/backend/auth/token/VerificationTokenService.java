package ua.dtsebulia.backend.auth.token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.dtsebulia.backend.config.JwtService;
import ua.dtsebulia.backend.user.User;
import ua.dtsebulia.backend.user.UserRepository;

import java.util.Calendar;
import java.util.HashMap;

@RequiredArgsConstructor
@Service
public class VerificationTokenService {

    private final VerificationTokenRepository tokenRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken token = tokenRepository.findByToken(oldToken);
        var verificationTokenTime = new VerificationToken();
        token.setToken(jwtService.generateToken(new HashMap<>(), token.getUser()));
        token.setExpirationTime(verificationTokenTime.getExpirationTime());
        return tokenRepository.save(token);
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

}
