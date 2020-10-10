package com.example.appname.service;

import com.example.appname.exceptions.BadCredentialsException;
import com.example.appname.exceptions.TokenNotFoundException;
import com.example.appname.model.Token;
import com.example.appname.model.TokenRequest;
import com.example.appname.model.TokenResponse;
import com.example.appname.repository.TokenRepository;
import com.example.appname.security.JwtTokenUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class AuthService {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private AppUserService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenRepository tokenRepository;

    public TokenResponse doAuthenticate(TokenRequest request) throws AccessDeniedException {
        val userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        log.info("User details {}", userDetails);
        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException();
        }

        val refreshToken = saveNewRefreshTokenAndReturnIt(userDetails);
        val token = jwtTokenUtil.generateToken(userDetails);
        return TokenResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .userId(userDetails.getUsername())
                .build();
    }

    public TokenResponse refreshToken(UserDetails userDetails, String token) {
        val userId = userDetails.getUsername();
        Token userToken = tokenRepository.findByToken(token);
        if (userToken == null) {
            throw new TokenNotFoundException();
        }

        if (!userToken.getUserId().equals(userId)) {
            log.warn("User {} tried to access user {} token", userId, userToken.getUserId());
            throw new TokenNotFoundException();
        }

        val accessToken = jwtTokenUtil.generateToken(userDetails);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(userToken.getToken())
                .userId(userDetails.getUsername())
                .build();
    }

    public void logout(UserDetails userDetails) {
        tokenRepository.deleteByUserId(userDetails.getUsername());
    }

    private String saveNewRefreshTokenAndReturnIt(UserDetails userDetails) {
        val refreshToken = generateNewToken();
        Token token = Token.builder()
                .token(refreshToken)
                .userId(userDetails.getUsername())
                .build();

        tokenRepository.save(token);
        return refreshToken;
    }

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
