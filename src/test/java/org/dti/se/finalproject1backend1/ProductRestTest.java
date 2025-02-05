package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
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
public class ProductRestTest extends TestConfiguration {

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
    public void testListAllProducts() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/products") // fakeproducts
                .param("page", "0")
                .param("size", "5"); // fake.size

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        List<ProductResponse> responseBody = objectMapper
                .readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

        assert responseBody != null;
    }

    @Test
    public void testGetProductById() throws Exception {
        Product realProduct = fakeProducts.getFirst();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/products/" + realProduct.getId());

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        ProductResponse responseBody = objectMapper
                .readValue(result.getResponse().getContentAsString(), ProductResponse.class);

        assert responseBody.getId().equals(realProduct.getId());
    }

    @Test
    public void testAddProduct() throws Exception {
        ProductRequest requestBody = new ProductRequest(UUID.randomUUID(), "New Product", "Description", 99.99, new byte[]{1, 2, 3});

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        ProductResponse responseBody = objectMapper
                .readValue(result.getResponse().getContentAsString(), ProductResponse.class);

        assert responseBody.getCategory().getId().equals(requestBody.getCategoryId());
        assert responseBody.getName().equals(requestBody.getName());
        assert responseBody.getDescription().equals(requestBody.getDescription());
        assert responseBody.getPrice().equals(requestBody.getPrice());
        assert responseBody.getImage().equals(requestBody.getImage());
    }

    @Test
    public void testUpdateProduct() throws Exception {
        Product realProduct = fakeProducts.getFirst();
        ProductRequest requestBody = new ProductRequest(UUID.randomUUID(), "Updated Name", "Updated Description", 150.0, new byte[]{4, 5, 6});

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/products/" + realProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        ProductResponse responseBody = objectMapper
                .readValue(result.getResponse().getContentAsString(), ProductResponse.class);

        assert responseBody.getCategory().getId().equals(requestBody.getCategoryId());
        assert responseBody.getName().equals(requestBody.getName());
        assert responseBody.getDescription().equals(requestBody.getDescription());
        assert responseBody.getPrice().equals(requestBody.getPrice());
        assert responseBody.getImage().equals(requestBody.getImage());
    }

    @Test
    public void testDeleteProduct() throws Exception {
        Product realProduct = fakeProducts.getFirst();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/products/" + realProduct.getId());

        mockMvc.perform(request).andExpect(status().isOk());
    }
}
