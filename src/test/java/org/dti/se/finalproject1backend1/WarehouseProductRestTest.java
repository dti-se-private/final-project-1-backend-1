package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseProduct;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WarehouseProductRestTest extends TestConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void beforeEach() throws Exception {
        populate();
    }

    @AfterEach
    public void afterEach() {
        depopulate();
    }

    @Test
    public void testListAllWarehouseProducts() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/warehouse-products")
                .param("page", "0")
                .param("size", "5");

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        List<WarehouseProductResponse> responseBody = objectMapper
                .readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

        assert responseBody != null;
    }

    @Test
    public void testGetWarehouseProductById() throws Exception {
        WarehouseProduct realProduct = fakeWarehouseProducts.getFirst();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/warehouse-products/" + realProduct.getId());

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        WarehouseProductResponse responseBody = objectMapper
                .readValue(result.getResponse().getContentAsString(), WarehouseProductResponse.class);

        assert responseBody.getId().equals(realProduct.getId());
    }

    @Test
    public void testAddWarehouseProduct() throws Exception {
        WarehouseProductRequest requestBody = new WarehouseProductRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                10.5
        );

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/warehouse-products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        WarehouseProductResponse responseBody = objectMapper
                .readValue(result.getResponse().getContentAsString(), WarehouseProductResponse.class);

        assert responseBody.getWarehouse().getId().equals(requestBody.getWarehouseId());
        assert responseBody.getProduct().getId().equals(requestBody.getProductId());
        assert responseBody.getQuantity().equals(requestBody.getQuantity());
    }

    @Test
    public void testUpdateWarehouseProduct() throws Exception {
        WarehouseProduct realProduct = fakeWarehouseProducts.getFirst();
        WarehouseProductRequest requestBody = new WarehouseProductRequest(
                realProduct.getWarehouse().getId(),
                realProduct.getProduct().getId(),
                20.0
        );

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/warehouse-products/" + realProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        WarehouseProductResponse responseBody = objectMapper
                .readValue(result.getResponse().getContentAsString(), WarehouseProductResponse.class);

        assert responseBody.getWarehouse().getId().equals(requestBody.getWarehouseId());
        assert responseBody.getProduct().getId().equals(requestBody.getProductId());
        assert responseBody.getQuantity().equals(requestBody.getQuantity());
    }

    @Test
    public void testDeleteWarehouseProduct() throws Exception {
        WarehouseProduct realProduct = fakeWarehouseProducts.getFirst();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/warehouse-products/" + realProduct.getId());

        mockMvc.perform(request).andExpect(status().isOk());
    }
}
