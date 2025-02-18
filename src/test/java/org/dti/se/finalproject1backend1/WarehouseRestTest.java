package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouses.WarehouseRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouses.WarehouseResponse;
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
public class WarehouseRestTest extends TestConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private GeometryFactory geometryFactory = new GeometryFactory();

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
    public void testAddWarehouse() throws Exception {
        Point location = geometryFactory.createPoint(new Coordinate(1.0, 1.0));
        WarehouseRequest request = WarehouseRequest.builder()
                .name("Main Warehouse")
                .description("Primary storage facility")
                .location(location)
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/warehouses")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<WarehouseResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Warehouse added.");
    }

    @Test
    public void testPatchWarehouse() throws Exception {
        Warehouse realWarehouse = fakeWarehouses.getFirst();
        Point location = geometryFactory.createPoint(new Coordinate(2.0, 2.0));
        WarehouseRequest request = WarehouseRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .description(String.format("description-%s", UUID.randomUUID()))
                .location(location)
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .patch("/warehouses/{warehouseId}", realWarehouse.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<WarehouseResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Warehouse patched.");
    }

    @Test
    public void testDeleteWarehouse() throws Exception {
        Warehouse realWarehouse = fakeWarehouses.getFirst();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .delete("/warehouses/{warehouseId}", realWarehouse.getId())
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
        assert body.getMessage().equals("Warehouse deleted.");
    }

    @Test
    public void testGetWarehouse() throws Exception {
        Warehouse realWarehouse = fakeWarehouses.getFirst();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/warehouses/{warehouseId}", realWarehouse.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<WarehouseResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Warehouse found.");
    }

    @Test
    public void testGetWarehouses() throws Exception {
        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/warehouses")
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
        ResponseBody<List<WarehouseResponse>> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Warehouses found.");
    }
}