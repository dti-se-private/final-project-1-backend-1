package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Verification;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.LoginByEmailAndPasswordRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterByEmailAndPasswordRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterByExternalRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @BeforeEach
    public void beforeEach() {
        populate();
    }

    @AfterEach
    public void afterEach() {
        depopulate();
    }

    @Test
    public void testRegisterByEmailAndPassword() throws Exception {
        RegisterByEmailAndPasswordRequest requestBody = RegisterByEmailAndPasswordRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .email(String.format("email-%s", UUID.randomUUID()))
                .password(rawPassword)
                .phone(String.format("phone-%s", UUID.randomUUID()))
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/authentications/registers/email-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Account> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Register succeed.");
        assert body.getData() != null;
        assert body.getData().getId() != null;
        assert body.getData().getName().equals(requestBody.getName());
        assert body.getData().getEmail().equals(requestBody.getEmail());
        assert securityConfiguration.matches(requestBody.getPassword(), body.getData().getPassword());
        assert body.getData().getPhone().equals(requestBody.getPhone());

        fakeAccounts.add(body.getData());
    }

    @Test
    public void testRegisterByInternal() throws Exception {
        String email = String.format("email-%s", UUID.randomUUID());
        String type = "REGISTER";

        Verification verification = getVerification(email, type);

        RegisterByEmailAndPasswordRequest requestBody = RegisterByEmailAndPasswordRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .email(email)
                .password(rawPassword)
                .phone(String.format("phone-%s", UUID.randomUUID()))
                .otp(verification.getCode())
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/authentications/registers/internal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Account> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Register succeed.");
        assert body.getData() != null;
        assert body.getData().getId() != null;
        assert body.getData().getName().equals(requestBody.getName());
        assert body.getData().getEmail().equals(requestBody.getEmail());
        assert securityConfiguration.matches(requestBody.getPassword(), body.getData().getPassword());
        assert body.getData().getPhone().equals(requestBody.getPhone());

        fakeAccounts.add(body.getData());
    }

    @Test
    public void registerByExternal() throws Exception {
        String mockIdToken = "mock-id-token";
        String email = String.format("email-%s", UUID.randomUUID());
        String name = String.format("name-%s", UUID.randomUUID());
        String picture = "http://example.com/picture.jpg";

        GoogleIdToken.Payload payload = Mockito.mock(GoogleIdToken.Payload.class);
        Mockito.when(payload.getEmail()).thenReturn(email);
        Mockito.when(payload.get("name")).thenReturn(name);
        Mockito.when(payload.get("picture")).thenReturn(picture.getBytes());

        GoogleIdToken idToken = Mockito.mock(GoogleIdToken.class);
        Mockito.when(idToken.getPayload()).thenReturn(payload);

        Mockito.when(googleIdTokenVerifier.verify(mockIdToken)).thenReturn(idToken);

        RegisterByExternalRequest requestBody = RegisterByExternalRequest
                .builder()
                .idToken(mockIdToken)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/authentications/registers/external")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Account> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Register succeed.");
        assert body.getData() != null;
        assert body.getData().getId() != null;
        assert body.getData().getName().equals(name);
        assert body.getData().getEmail().equals(email);
        assert body.getData().getImage() != null;

        fakeAccounts.add(body.getData());
    }

    @Test
    public void testLoginByEmailAndPassword() throws Exception {
        ResponseBody<Account> registerResponse = register();
        Account realAccount = registerResponse.getData();
        LoginByEmailAndPasswordRequest requestBody = LoginByEmailAndPasswordRequest
                .builder()
                .email(realAccount.getEmail())
                .password(rawPassword)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/authentications/logins/email-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
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
    public void testLogout() throws Exception {
        ResponseBody<Account> registerResponse = register();
        Account realAccount = registerResponse.getData();
        ResponseBody<Session> loginResponse = login(realAccount);
        Session requestBody = loginResponse.getData();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/authentications/logouts/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
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
        ResponseBody<Account> registerResponse = register();
        Account realAccount = registerResponse.getData();
        ResponseBody<Session> loginResponse = login(realAccount);
        Session requestBody = loginResponse.getData();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/authentications/refreshes/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
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