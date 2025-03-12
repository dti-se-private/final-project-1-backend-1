package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseProduct;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseProductNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
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
    public void testGetWarehouseProducts() throws Exception {
        List<WarehouseProduct> realWarehouseProducts = fakeWarehouseProducts;

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/warehouse-products")
                .param("page", "0")
                .param("size", String.valueOf(realWarehouseProducts.size()))
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<List<WarehouseProductResponse>> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Warehouse products found.");
        assert responseBody.getData().size() == realWarehouseProducts.size();
    }

    @Test
    public void testGetWarehouseProduct() throws Exception {
        WarehouseProduct realWarehouseProduct = fakeWarehouseProducts
                .stream()
                .findFirst()
                .orElseThrow(WarehouseProductNotFoundException::new);

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/warehouse-products/{warehouseProductId}", realWarehouseProduct.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<WarehouseProductResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Warehouse product found.");
        assert responseBody.getData().getId().equals(realWarehouseProduct.getId());
    }

    @Test
    public void testAddWarehouseProduct() throws Exception {
        Product newProduct = Product
                .builder()
                .id(UUID.randomUUID())
                .category(fakeCategories.getFirst())
                .name(String.format("name-%s", UUID.randomUUID()))
                .description(String.format("description-%s", UUID.randomUUID()))
                .price(Math.ceil(Math.random() * 1000000))
                .weight(Math.ceil(Math.random() * 10000))
                .image(null)
                .build();
        fakeProducts.add(newProduct);
        productRepository.saveAndFlush(newProduct);
        Product realProduct = fakeProducts.getLast();

        GeometryFactory geometryFactory = new GeometryFactory();
        Warehouse newWarehouse = Warehouse
                .builder()
                .id(UUID.randomUUID())
                .name(String.format("name-%s", UUID.randomUUID()))
                .description(String.format("description-%s", UUID.randomUUID()))
                .location(geometryFactory.createPoint(new Coordinate(Math.random() * 10, Math.random() * 10)))
                .build();
        fakeWarehouses.add(newWarehouse);
        warehouseRepository.saveAndFlush(newWarehouse);
        Warehouse realWarehouse = fakeWarehouses.getLast();

        WarehouseProductRequest requestBody = WarehouseProductRequest
                .builder()
                .productId(realProduct.getId())
                .warehouseId(realWarehouse.getId())
                .quantity(Math.ceil(Math.random() * 1000))
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/warehouse-products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isCreated())
                .andReturn();

        ResponseBody<WarehouseProductResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Warehouse product added.");
        assert responseBody.getData().getId() != null;

        WarehouseProduct savedWarehouseProduct = warehouseProductRepository
                .findById(responseBody.getData().getId())
                .orElseThrow(WarehouseProductNotFoundException::new);
        fakeWarehouseProducts.add(savedWarehouseProduct);
    }

    @Test
    public void testPatchWarehouseProduct() throws Exception {
        Product realProduct = fakeProducts.getFirst();
        Warehouse realWarehouse = fakeWarehouses.getFirst();

        WarehouseProduct realWarehouseProduct = fakeWarehouseProducts
                .stream()
                .findFirst()
                .orElseThrow(WarehouseProductNotFoundException::new);

        WarehouseProductRequest requestBody = WarehouseProductRequest
                .builder()
                .productId(realProduct.getId())
                .warehouseId(realWarehouse.getId())
                .quantity(Math.ceil(Math.random() * 1000))
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .patch("/warehouse-products/{warehouseProductId}", realWarehouseProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<WarehouseProductResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Warehouse product patched.");
        assert responseBody.getData().getId().equals(realWarehouseProduct.getId());

        WarehouseProduct patchedWarehouseProduct = warehouseProductRepository
                .findById(responseBody.getData().getId())
                .orElseThrow(WarehouseProductNotFoundException::new);

        fakeWarehouseProducts.set(fakeWarehouseProducts.indexOf(realWarehouseProduct), patchedWarehouseProduct);
    }

    @Test
    public void testDeleteWarehouseProduct() throws Exception {
        WarehouseProduct realWarehouseProduct = fakeWarehouseProducts.getFirst();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .delete("/warehouse-products/{warehouseProductId}", realWarehouseProduct.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<WarehouseProductResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Warehouse product deleted.");
        assert productRepository.findById(realWarehouseProduct.getId()).isEmpty();
        fakeWarehouseProducts.remove(realWarehouseProduct);
    }
}