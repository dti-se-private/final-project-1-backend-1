package org.dti.se.finalproject1backend1;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Session;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.LoginByEmailAndPasswordRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public class AuthenticationRestTest extends TestConfiguration {

    @BeforeEach
    public void beforeEach() {
        configure();
        populate();
    }

    @AfterEach
    public void afterEach() {
        depopulate();
    }

    @Test
    public void testLoginByEmailAndPassword() {
        ResponseBody<Account> registerResponse = register();
        Account realAccount = registerResponse.getData();
        LoginByEmailAndPasswordRequest requestBody = LoginByEmailAndPasswordRequest
                .builder()
                .email(realAccount.getEmail())
                .password(rawPassword)
                .build();

        webTestClient
                .post()
                .uri("/authentications/logins/email-password")
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<Session>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage().equals("Login succeed.");
                    assert body.getData() != null;
                    assert body.getData().getAccessToken() != null;
                    assert body.getData().getRefreshToken() != null;
                    assert body.getData().getAccessTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
                    assert body.getData().getRefreshTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
                });

        fakeAccounts.add(realAccount);
    }

    @Test
    public void testLogout() {
        ResponseBody<Account> registerResponse = register();
        Account realAccount = registerResponse.getData();
        ResponseBody<Session> loginResponse = login(realAccount);
        Session requestBody = loginResponse.getData();

        webTestClient
                .post()
                .uri("/authentications/logouts/session")
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<Void>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage().equals("Logout succeed.");
                });

        fakeAccounts.add(realAccount);
    }

    @Test
    public void testRefreshSession() {
        ResponseBody<Account> registerResponse = register();
        Account realAccount = registerResponse.getData();
        ResponseBody<Session> loginResponse = login(realAccount);
        Session requestBody = loginResponse.getData();

        webTestClient
                .post()
                .uri("/authentications/refreshes/session")
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<Session>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getData() != null;
                    assert body.getData().getAccessToken() != null;
                    assert body.getData().getRefreshToken() != null;
                    assert body.getData().getAccessTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
                    assert body.getData().getRefreshTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
                });

        fakeAccounts.add(realAccount);
    }
}
