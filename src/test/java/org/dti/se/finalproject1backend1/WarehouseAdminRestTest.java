package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.admin.WarehouseAdminRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.admin.WarehouseAdminResponse;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WarehouseAdminRestTest extends TestConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    public void testAssignWarehouseAdmin() throws Exception {
        Account realAccount = fakeAccounts.getFirst();
        WarehouseAdminRequest request = WarehouseAdminRequest.builder()
                .accountId(realAccount.getId())
                .warehouseId(fakeWarehouses.getFirst().getId())
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/warehouse-admins")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<WarehouseAdminResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Warehouse admin assigned.");
    }

    @Test
    public void testGetWarehouseAdmin() throws Exception {
        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/warehouse-admins/" + fakeWarehouseAdmins.getFirst().getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<WarehouseAdminResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Warehouse admin found.");
    }

    @Test
    public void testUpdateWarehouseAdmin() throws Exception {
        Account realAccount = fakeAccounts.getFirst();
        WarehouseAdminRequest request = WarehouseAdminRequest.builder()
                .accountId(realAccount.getId())
                .warehouseId(fakeWarehouses.getLast().getId())
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .patch("/warehouse-admins/" + fakeWarehouseAdmins.getFirst().getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<WarehouseAdminResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Warehouse admin updated.");
    }

    @Test
    public void testDeleteWarehouseAdmin() throws Exception {
        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .delete("/warehouse-admins/" + fakeWarehouseAdmins.getFirst().getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Void> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Warehouse admin deleted.");
    }
}