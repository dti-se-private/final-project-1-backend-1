package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.products.CategoryNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.products.ProductNotFoundException;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductRestTest extends TestConfiguration {

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
    public void testGetProducts() throws Exception {
        List<Product> realProducts = fakeProducts;

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/products")
                .param("page", "0")
                .param("size", String.valueOf(realProducts.size()))
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<List<ProductResponse>> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Products found.");
        assert responseBody.getData().size() == realProducts.size();
    }

    @Test
    public void testGetProduct() throws Exception {
        Product realProduct = fakeProducts
                .stream()
                .findFirst()
                .orElseThrow(ProductNotFoundException::new);

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/products/{productId}", realProduct.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<ProductResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Product found.");
        assert responseBody.getData().getId().equals(realProduct.getId());
        assert responseBody.getData().getName().equals(realProduct.getName());
        assert responseBody.getData().getDescription().equals(realProduct.getDescription());
        assert responseBody.getData().getPrice().equals(realProduct.getPrice());
        assert responseBody.getData().getWeight().equals(realProduct.getWeight());
        assert Arrays.equals(responseBody.getData().getImage(), realProduct.getImage());
        assert responseBody.getData().getCategory().getId().equals(realProduct.getCategory().getId());
        assert responseBody.getData().getCategory().getName().equals(realProduct.getCategory().getName());
        assert responseBody.getData().getCategory().getDescription().equals(realProduct.getCategory().getDescription());
    }

    @Test
    public void testAddProduct() throws Exception {
        Category realCategory = fakeCategories
                .stream()
                .findFirst()
                .orElseThrow(CategoryNotFoundException::new);

        ProductRequest requestBody = ProductRequest
                .builder()
                .categoryId(realCategory.getId())
                .name(String.format("name-%s", UUID.randomUUID()))
                .description(String.format("description-%s", UUID.randomUUID()))
                .price(Math.ceil(Math.random() * 1000000))
                .weight(Math.ceil(Math.random() * 10000))
                .image(null)
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isCreated())
                .andReturn();

        ResponseBody<ProductResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Product added.");
        assert responseBody.getData().getId() != null;
        assert responseBody.getData().getName().equals(requestBody.getName());
        assert responseBody.getData().getDescription().equals(requestBody.getDescription());
        assert responseBody.getData().getPrice().equals(requestBody.getPrice());
        assert responseBody.getData().getWeight().equals(requestBody.getWeight());
        assert Arrays.equals(responseBody.getData().getImage(), requestBody.getImage());
        assert responseBody.getData().getCategory().getId().equals(realCategory.getId());
        assert responseBody.getData().getCategory().getName().equals(realCategory.getName());
        assert responseBody.getData().getCategory().getDescription().equals(realCategory.getDescription());

        Product savedProduct = productRepository
                .findById(responseBody.getData().getId())
                .orElseThrow(ProductNotFoundException::new);
        fakeProducts.add(savedProduct);
    }

    @Test
    public void testPatchProduct() throws Exception {
        Category realCategory = fakeCategories
                .stream()
                .findFirst()
                .orElseThrow(CategoryNotFoundException::new);

        Product realProduct = fakeProducts
                .stream()
                .findFirst()
                .orElseThrow(ProductNotFoundException::new);

        ProductRequest requestBody = ProductRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .description(String.format("description-%s", UUID.randomUUID()))
                .price(Math.ceil(Math.random() * 1000000))
                .weight(Math.ceil(Math.random() * 10000))
                .image(null)
                .categoryId(realCategory.getId())
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .patch("/products/{productId}", realProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<ProductResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Product patched.");
        assert responseBody.getData().getId().equals(realProduct.getId());
        assert responseBody.getData().getName().equals(requestBody.getName());
        assert responseBody.getData().getDescription().equals(requestBody.getDescription());
        assert responseBody.getData().getPrice().equals(requestBody.getPrice());
        assert responseBody.getData().getWeight().equals(requestBody.getWeight());
        assert Arrays.equals(responseBody.getData().getImage(), requestBody.getImage());
        assert responseBody.getData().getCategory().getId().equals(realCategory.getId());
        assert responseBody.getData().getCategory().getName().equals(realCategory.getName());
        assert responseBody.getData().getCategory().getDescription().equals(realCategory.getDescription());

        Product patchedProduct = productRepository
                .findById(responseBody.getData().getId())
                .orElseThrow(ProductNotFoundException::new);

        fakeProducts.set(fakeProducts.indexOf(realProduct), patchedProduct);
    }

    @Test
    public void testDeleteProduct() throws Exception {
        Product realProduct = fakeProducts
                .stream()
                .findFirst()
                .orElseThrow(ProductNotFoundException::new);

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .delete("/products/{productId}", realProduct.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<ProductResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Product deleted.");
        assert productRepository.findById(realProduct.getId()).isEmpty();
        fakeProducts.remove(realProduct);
    }
}