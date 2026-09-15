package com.marcosperboni.integrationbff.web;

import static org.mockito.BDDMockito.given;

import com.marcosperboni.integrationbff.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(controllers = AuthController.class, excludeAutoConfiguration = ReactiveWebSecurityAutoConfiguration.class)
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void issueTokenReturnsBearerTokenForValidCredentials() {
        given(jwtService.issueToken("demo")).willReturn("signed-jwt");
        given(jwtService.expirationSeconds()).willReturn(1800L);

        webTestClient.post().uri("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"demo\",\"password\":\"demo123\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("signed-jwt")
                .jsonPath("$.tokenType").isEqualTo("Bearer");
    }

    @Test
    void issueTokenRejectsInvalidCredentials() {
        webTestClient.post().uri("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"demo\",\"password\":\"wrong\"}")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("UNAUTHORIZED");
    }

    @Test
    void issueTokenRejectsBlankUsername() {
        webTestClient.post().uri("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"\",\"password\":\"demo123\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("VALIDATION_ERROR");
    }
}
