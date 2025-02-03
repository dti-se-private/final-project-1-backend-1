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
    public void testGetAllWarehouseProducts() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/warehouse-products")
                .param("page", "0")
                .param("size", "5");

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        List<WarehouseProductResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        assert responseBody != null;
        assert !responseBody.isEmpty();
    }

    @Test
    public void testGetWarehouseProductById() throws Exception {
        UUID productId = fakeWarehouseProducts.getFirst().getId();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/warehouse-products/" + productId);

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        WarehouseProductResponse responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                WarehouseProductResponse.class
        );

        assert responseBody.getId().equals(productId);
    }

    @Test
    public void testAddWarehouseProduct() throws Exception {
        WarehouseProductRequest requestBody = new WarehouseProductRequest("New Product", "New Description");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/warehouse-products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        WarehouseProduct responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                WarehouseProduct.class
        );

        assert responseBody.getName().equals(requestBody.getName());
        assert responseBody.getDescription().equals(requestBody.getDescription());
    }

    @Test
    public void testUpdateWarehouseProduct() throws Exception {
        UUID productId = fakeWarehouseProducts.getFirst().getId();
        WarehouseProductRequest requestBody = new WarehouseProductRequest("Updated Product", "Updated Description");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/warehouse-products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        WarehouseProductResponse responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                WarehouseProductResponse.class
        );

        assert responseBody.getName().equals(requestBody.getName());
        assert responseBody.getDescription().equals(requestBody.getDescription());
    }

    @Test
    public void testDeleteWarehouseProduct() throws Exception {
        UUID productId = fakeWarehouseProducts.getFirst().getId();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/warehouse-products/" + productId);

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assert responseBody.equals("Warehouse product deleted Successfully.");
    }
}
