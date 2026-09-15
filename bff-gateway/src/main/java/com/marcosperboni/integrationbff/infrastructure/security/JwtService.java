package com.marcosperboni.integrationbff.infrastructure.security;

import com.marcosperboni.integrationbff.config.properties.JwtProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Component;

/**
 * Issues demo access tokens signed with the shared HS256 secret that
 * {@link com.marcosperboni.integrationbff.config.SecurityConfig} configures
 * the resource server to validate against.
 */
@Component
public class JwtService {

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String issueToken(String subject) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issuer("enterprise-integration-bff")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(Duration.ofMinutes(properties.expirationMinutes()))))
                    .build();
            SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJwt.sign(new MACSigner(properties.secret().getBytes(StandardCharsets.UTF_8)));
            return signedJwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to issue JWT", e);
        }
    }

    public long expirationSeconds() {
        return Duration.ofMinutes(properties.expirationMinutes()).toSeconds();
    }
}
