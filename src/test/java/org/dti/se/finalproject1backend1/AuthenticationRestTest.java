package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Verification;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.*;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationRestTest extends TestConfiguration {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void beforeEach() {
        populate();
    }

    @AfterEach
    public void afterEach() {
        depopulate();
    }

    @Test
    public void testRegisterByInternal() throws Exception {
        String email = String.format("email-%s", UUID.randomUUID());
        String type = "REGISTER";

        Verification verification = getVerification(email, type);

        RegisterByInternalRequest requestBody = RegisterByInternalRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .email(email)
                .password(rawPassword)
                .phone(String.format("phone-%s", UUID.randomUUID()))
                .otp(verification.getCode())
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/authentications/registers/internal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<AccountResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Register by internal succeed.");
        assert body.getData() != null;
        assert body.getData().getId() != null;
        assert body.getData().getName().equals(requestBody.getName());
        assert body.getData().getEmail().equals(requestBody.getEmail());
        assert securityConfiguration.matches(requestBody.getPassword(), body.getData().getPassword());
        assert body.getData().getPhone().equals(requestBody.getPhone());
        assert body.getData().getImage() == null;
        assert body.getData().getIsVerified().equals(true);

        Account registeredAccount = accountRepository
                .findById(body.getData().getId())
                .orElseThrow(AccountNotFoundException::new);
        fakeAccounts.add(registeredAccount);
    }

    @Test
    @ResourceLock("googleTokenMock")
    public void testRegisterByExternal() throws Exception {
        String email = String.format("email-%s", UUID.randomUUID());
        String name = String.format("name-%s", UUID.randomUUID());
        String picture = "https://placehold.co/400x400";

        GoogleTokenResponse tokenResponse = Mockito.mock(GoogleTokenResponse.class);
        Mockito.when(googleConfiguration.getToken(Mockito.any())).thenReturn(tokenResponse);
        GoogleIdToken googleIdToken = Mockito.mock(GoogleIdToken.class);
        Mockito.when(tokenResponse.parseIdToken()).thenReturn(googleIdToken);
        GoogleIdToken.Payload payload = Mockito.mock(GoogleIdToken.Payload.class);
        Mockito.when(payload.getEmail()).thenReturn(email);
        Mockito.when(payload.get("name")).thenReturn(name);
        Mockito.when(payload.get("picture")).thenReturn(picture);
        Mockito.when(googleIdToken.getPayload()).thenReturn(payload);

        RegisterByExternalRequest requestBody = RegisterByExternalRequest
                .builder()
                .authorizationCode(UUID.randomUUID().toString())
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/authentications/registers/external")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Account> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Register by external succeed.");
        assert body.getData() != null;
        assert body.getData().getId() != null;
        assert body.getData().getName().equals(name);
        assert body.getData().getEmail().equals(email);
        assert body.getData().getPassword() == null;
        assert body.getData().getPhone() == null;
        assert body.getData().getImage() != null;
        assert body.getData().getIsVerified().equals(true);

        fakeAccounts.add(body.getData());
    }

    @Test
    public void testLoginByInternal() throws Exception {
        ResponseBody<AccountResponse> registerResponse = registerByInternal();
        Account realAccount = accountRepository
                .findById(registerResponse.getData().getId())
                .orElseThrow(AccountNotFoundException::new);
        LoginByInternalRequest requestBody = LoginByInternalRequest
                .builder()
                .email(realAccount.getEmail())
                .password(rawPassword)
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/authentications/logins/internal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Session> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Login succeed.");
        assert body.getData() != null;
        assert body.getData().getAccessToken() != null;
        assert body.getData().getRefreshToken() != null;
        assert body.getData().getAccessTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
        assert body.getData().getRefreshTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));

        fakeAccounts.add(realAccount);
    }

    @Test
    @ResourceLock("googleTokenMock")
    public void testLoginByExternal() throws Exception {
        ResponseBody<Account> registerResponse = registerByExternal();
        Account realAccount = registerResponse.getData();

        LoginByExternalRequest requestBody = LoginByExternalRequest
                .builder()
                .authorizationCode(UUID.randomUUID().toString())
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/authentications/logins/external")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Session> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Login succeed.");
        assert body.getData() != null;
        assert body.getData().getAccessToken() != null;
        assert body.getData().getRefreshToken() != null;
        assert body.getData().getAccessTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
        assert body.getData().getRefreshTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));

        fakeAccounts.add(realAccount);
    }

    @Test
    public void testResetPassword() throws Exception {
        ResponseBody<AccountResponse> registerResponse = registerByInternal();
        Account realAccount = accountRepository
                .findById(registerResponse.getData().getId())
                .orElseThrow(AccountNotFoundException::new);
        String newRawPassword = "new-password";

        Verification verification = getVerification(realAccount.getEmail(), "RESET_PASSWORD");

        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest
                .builder()
                .email(realAccount.getEmail())
                .otp(verification.getCode())
                .newPassword(newRawPassword)
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/authentications/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetPasswordRequest));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Void> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Password reset succeed.");
        assert body.getData() == null;

        Account resetPasswordAccount = Account
                .builder()
                .id(realAccount.getId())
                .name(realAccount.getName())
                .email(realAccount.getEmail())
                .password(realAccount.getPassword())
                .phone(realAccount.getPhone())
                .image(realAccount.getImage())
                .build();
        fakeAccounts.set(fakeAccounts.indexOf(realAccount), resetPasswordAccount);
    }

    @Test
    public void testLogout() throws Exception {
        ResponseBody<AccountResponse> registerResponse = registerByInternal();
        Account realAccount = accountRepository
                .findById(registerResponse.getData().getId())
                .orElseThrow(AccountNotFoundException::new);
        ResponseBody<Session> loginResponse = loginByInternal(realAccount);
        Session requestBody = loginResponse.getData();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/authentications/logouts/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Void> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Logout succeed.");

        fakeAccounts.add(realAccount);
    }

    @Test
    public void testRefreshSession() throws Exception {
        ResponseBody<AccountResponse> registerResponse = registerByInternal();
        Account realAccount = accountRepository
                .findById(registerResponse.getData().getId())
                .orElseThrow(AccountNotFoundException::new);
        ResponseBody<Session> loginResponse = loginByInternal(realAccount);
        Session requestBody = loginResponse.getData();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/authentications/refreshes/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Session> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getData() != null;
        assert body.getData().getAccessToken() != null;
        assert body.getData().getRefreshToken() != null;
        assert body.getData().getAccessTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
        assert body.getData().getRefreshTokenExpiredAt().isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));

        fakeAccounts.add(realAccount);
    }
}