package com.example.appname.controller;

import com.example.appname.exceptions.NoTokenSuppliedException;
import com.example.appname.model.*;
import com.example.appname.security.JwtTokenUtil;
import com.example.appname.service.AppUserService;
import com.example.appname.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.AccessDeniedException;


@CrossOrigin
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class AuthenticationController {
    @Autowired private AuthService authService;
    @Autowired private JwtTokenUtil jwtTokenUtil;

    private final String REFRESH_TOKEN_KEY = "X-Refresh-Token";

    @PostMapping("/authenticate")
    public TokenResponse doAuthenticate(
            @RequestBody TokenRequest request,
            HttpServletResponse response
    ) throws AccessDeniedException {
        val tokenResponse = authService.doAuthenticate(request);
        Cookie cookie = new Cookie(REFRESH_TOKEN_KEY, tokenResponse.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
//        cookie.setSecure(true); TODO: production
        response.addCookie(cookie);
        return tokenResponse;
    }

    @PostMapping("/refresh")
    public TokenResponse refreshToken(
            @CookieValue(value = REFRESH_TOKEN_KEY, defaultValue = "") String refreshToken,
            @RequestParam("refresh_token") String paramRefreshToken,
            HttpServletRequest request
    ) {
        refreshToken = refreshToken.isEmpty() ? paramRefreshToken : refreshToken;
        if (refreshToken == null) {
            throw new NoTokenSuppliedException();
        }

        String userId = jwtTokenUtil.getUserIdFromToken(request);
        UserDetails userDetails = new AppUserPrincipal(AppUser.builder().id(userId).build());
        return authService.refreshToken(userDetails, refreshToken);
    }

    @GetMapping("/logout")
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String userId = jwtTokenUtil.getUserIdFromToken(request);
        UserDetails userDetails = new AppUserPrincipal(AppUser.builder().id(userId).build());
        Cookie cookie = new Cookie(REFRESH_TOKEN_KEY, null); // Not necessary, but saves bandwidth.
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        authService.logout(userDetails);
    }
}
