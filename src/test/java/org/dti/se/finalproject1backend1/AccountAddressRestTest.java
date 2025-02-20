package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountAddress;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountAddressRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountAddressResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountAddressRestTest extends TestConfiguration {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    GeometryFactory geometryFactory = new GeometryFactory();

    @BeforeEach
    public void beforeEach() throws Exception {
        populate();
        Account selectedAccount = fakeAccounts.getFirst();
        auth(selectedAccount);
    }

    @AfterEach
    public void afterEach() {
        depopulate();
    }

    @Test
    public void testAddAddress() throws Exception {
        Point location = geometryFactory.createPoint(new Coordinate(1.0, 1.0));
        AccountAddressRequest request = AccountAddressRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .address(String.format("address-%s", UUID.randomUUID()))
                .isPrimary(true)
                .location(location)
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/account-addresses")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<AccountAddressResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Address added.");
    }

    @Test
    public void testPatchAddress() throws Exception {
        AccountAddress realAddress = fakeAccountAddresses.getFirst();
        Point location = geometryFactory.createPoint(new Coordinate(2.0, 2.0));
        AccountAddressRequest request = AccountAddressRequest.builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .address(String.format("address-%s", UUID.randomUUID()))
                .isPrimary(false)
                .location(location)
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .patch("/account-addresses/" + realAddress.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<AccountAddressResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Address patched.");
    }

    @Test
    public void testDeleteAddress() throws Exception {
        AccountAddress realAddress = fakeAccountAddresses.getFirst();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .delete("/account-addresses/" + realAddress.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Void> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Address deleted.");
    }

    @Test
    public void testGetAddress() throws Exception {
        AccountAddress realAddress = fakeAccountAddresses.getFirst();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/account-addresses/" + realAddress.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<AccountAddressResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Address found.");
    }

    @Test
    public void testGetAddresses() throws Exception {
        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/account-addresses")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .param("page", "0")
                .param("size", "10")
                .param("search", "");

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<List<AccountAddressResponse>> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Addresses found.");
    }
}