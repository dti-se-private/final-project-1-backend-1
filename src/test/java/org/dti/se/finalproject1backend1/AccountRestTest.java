package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Verification;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountRestTest extends TestConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void beforeEach() throws Exception {
        populate();
        auth();
    }

    @AfterEach
    public void afterEach() {
        depopulate();
    }

    @Test
    public void testSaveOne() throws Exception {
        String encodedPassword = securityConfiguration.encode(rawPassword);
        AccountRequest accountCreator = AccountRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .email(String.format("email-%s", UUID.randomUUID()))
                .password(rawPassword)
                .phone(String.format("phone-%s", UUID.randomUUID()))
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountCreator))
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<AccountResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Account saved.");
        assert body.getData() != null;
        assert body.getData().getId() != null;
        assert body.getData().getName().equals(accountCreator.getName());
        assert body.getData().getEmail().equals(accountCreator.getEmail());
        assert body.getData().getPassword().equals(encodedPassword);
        assert body.getData().getPhone().equals(accountCreator.getPhone());

        Account savedAccount = Account
                .builder()
                .id(body.getData().getId())
                .name(body.getData().getName())
                .email(body.getData().getEmail())
                .password(body.getData().getPassword())
                .phone(body.getData().getPhone())
                .image(body.getData().getImage())
                .build();
        fakeAccounts.add(savedAccount);
    }

    @Test
    public void testFindOneById() throws Exception {
        Account realAccount = fakeAccounts.getFirst();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/accounts/{id}", realAccount.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<AccountResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Account found.");
        assert body.getData() != null;
        assert body.getData().getId().equals(realAccount.getId());
        assert body.getData().getName().equals(realAccount.getName());
        assert body.getData().getEmail().equals(realAccount.getEmail());
        assert body.getData().getPassword().equals(realAccount.getPassword());
        assert body.getData().getPhone().equals(realAccount.getPhone());
    }

    @Test
    public void testPatchOneById() throws Exception {
        Account realAccount = fakeAccounts.getFirst();
        String encodedPassword = securityConfiguration.encode(rawPassword);

        String newEmail = "new-email@example.com";
        String otpType = "UPDATE_ACCOUNT";
        Verification verification = getVerification(newEmail, otpType);
        String updateEmailOtp = verification.getCode();

        AccountRequest accountPatcher = AccountRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .email(newEmail)
                .otp(updateEmailOtp)
                .password(rawPassword)
                .phone(String.format("phone-%s", UUID.randomUUID()))
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch("/accounts/{id}", realAccount.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountPatcher))
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<AccountResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Account patched.");
        assert body.getData() != null;
        assert body.getData().getId().equals(realAccount.getId());
        assert body.getData().getName().equals(accountPatcher.getName());
        assert body.getData().getEmail().equals(accountPatcher.getEmail());
        assert body.getData().getPassword().equals(encodedPassword);
        assert body.getData().getPhone().equals(accountPatcher.getPhone());

        Account patchedAccount = Account
                .builder()
                .id(body.getData().getId())
                .name(body.getData().getName())
                .email(body.getData().getEmail())
                .password(body.getData().getPassword())
                .phone(body.getData().getPhone())
                .image(body.getData().getImage())
                .build();
        fakeAccounts.set(fakeAccounts.indexOf(realAccount), patchedAccount);
    }

    @Test
    public void testDeleteOneById() throws Exception {
        Account realAccount = fakeAccounts.getFirst();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/accounts/{id}", realAccount.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Account> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Account deleted.");
        assert body.getData() == null;

        fakeAccounts.remove(realAccount);
    }
}