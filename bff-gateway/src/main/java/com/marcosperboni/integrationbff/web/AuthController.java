package com.marcosperboni.integrationbff.web;

import com.marcosperboni.integrationbff.infrastructure.security.JwtService;
import com.marcosperboni.integrationbff.web.dto.LoginRequest;
import com.marcosperboni.integrationbff.web.dto.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Demo-only login: a single hardcoded user, just enough to exercise the JWT
 * issuance and propagation pattern for this portfolio project. A real BFF
 * would delegate authentication to an identity provider.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String DEMO_USERNAME = "demo";
    private static final String DEMO_PASSWORD = "demo123";

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    public Mono<TokenResponse> issueToken(@Valid @RequestBody LoginRequest request) {
        if (!DEMO_USERNAME.equals(request.username()) || !DEMO_PASSWORD.equals(request.password())) {
            return Mono.error(new BadCredentialsException("Invalid credentials"));
        }
        String token = jwtService.issueToken(request.username());
        return Mono.just(TokenResponse.of(token, jwtService.expirationSeconds()));
    }
}
