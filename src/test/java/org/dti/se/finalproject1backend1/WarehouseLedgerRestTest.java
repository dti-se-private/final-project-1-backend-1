package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseLedger;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseProduct;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.AddMutationRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.ApprovalMutationRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.WarehouseLedgerResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseLedgerNotFoundException;
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

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WarehouseLedgerRestTest extends TestConfiguration {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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
    public void testGetStockMutationRequests() throws Exception {
        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/warehouse-ledgers/mutations")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .param("page", "0")
                .param("size", "10")
                .param("search", "")
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<List<WarehouseLedgerResponse>> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Stock mutation requests found.");
    }

    @Test
    public void testGetStockMutationRequest() throws Exception {
        WarehouseLedger realWarehouseLedger = fakeWarehouseLedgers.getFirst();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/warehouse-ledgers/mutations/{warehouseLedgerId}", realWarehouseLedger.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<WarehouseLedgerResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Stock mutation request found.");
        assert body.getData().getId().equals(realWarehouseLedger.getId());
    }

    @Test
    public void testAddStockMutationRequest() throws Exception {
        WarehouseProduct realOriginWarehouseProduct = fakeWarehouseProducts.getFirst();
        WarehouseProduct realDestinationWarehouseProduct = fakeWarehouseProducts.
                stream()
                .filter(warehouseProduct -> !warehouseProduct.getWarehouse().getId().equals(realOriginWarehouseProduct.getWarehouse().getId()))
                .findFirst()
                .orElseThrow();
        AddMutationRequest requestBody = AddMutationRequest
                .builder()
                .productId(realOriginWarehouseProduct.getProduct().getId())
                .originWarehouseId(realOriginWarehouseProduct.getWarehouse().getId())
                .destinationWarehouseId(realDestinationWarehouseProduct.getWarehouse().getId())
                .quantity(Math.ceil(Math.random() * 100))
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/warehouse-ledgers/mutations/add")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .content(objectMapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Void> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Stock mutation request added.");
    }

    @Test
    public void testApproveStockMutationRequest() throws Exception {
        WarehouseLedger realWarehouseLedger = fakeWarehouseLedgers
                .stream()
                .filter(warehouseLedger -> warehouseLedger.getStatus().equals("WAITING_FOR_APPROVAL"))
                .filter(warehouseLedger -> !warehouseLedger.getOriginWarehouseProduct().getWarehouse().getId().equals(warehouseLedger.getDestinationWarehouseProduct().getWarehouse().getId()))
                .findFirst()
                .orElseThrow(WarehouseLedgerNotFoundException::new);

        ApprovalMutationRequest requestBody = ApprovalMutationRequest
                .builder()
                .warehouseLedgerId(realWarehouseLedger.getId())
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/warehouse-ledgers/mutations/approve", realWarehouseLedger.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .content(objectMapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Void> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Stock mutation request approved.");
    }


    @Test
    public void testRejectStockMutationRequest() throws Exception {
        WarehouseLedger realWarehouseLedger = fakeWarehouseLedgers
                .stream()
                .filter(warehouseLedger -> warehouseLedger.getStatus().equals("WAITING_FOR_APPROVAL"))
                .filter(warehouseLedger -> !warehouseLedger.getOriginWarehouseProduct().getWarehouse().getId().equals(warehouseLedger.getDestinationWarehouseProduct().getWarehouse().getId()))
                .findFirst()
                .orElseThrow(WarehouseLedgerNotFoundException::new);

        ApprovalMutationRequest requestBody = ApprovalMutationRequest
                .builder()
                .warehouseLedgerId(realWarehouseLedger.getId())
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/warehouse-ledgers/mutations/reject", realWarehouseLedger.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .content(objectMapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Void> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Stock mutation request rejected.");
    }
}