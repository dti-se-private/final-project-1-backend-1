package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
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
public class CategoryRestTest extends TestConfiguration {

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
    public void testGetAllCategories() throws Exception {
        List<Category> realCategories = fakeCategories
                .stream()
                .toList();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/product-categories")
                .param("page", "0")
                .param("size", String.valueOf(realCategories.size()));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        List<CategoryResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        assert responseBody != null;
        assert !responseBody.isEmpty();
    }

    @Test
    public void testGetCategoryById() throws Exception {
        UUID categoryId = fakeCategories.getFirst().getId();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/product-categories/" + categoryId);

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        CategoryResponse responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CategoryResponse.class
        );

        assert responseBody.getId().equals(categoryId);
    }

    @Test
    public void testAddCategory() throws Exception {
        Category fakeCategory = fakeCategories
                .stream()
                .findFirst()
                .orElse(null);

        assert fakeCategory != null;

        CategoryRequest requestBody = new CategoryRequest(fakeCategory.getName(), fakeCategory.getDescription());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/product-categories")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken()) // Add this if authentication is required
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk()) // Change to isCreated() if returning 201
                .andReturn();

        // Adjust response parsing based on what the controller returns
        Category responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Category.class
        );

        assert responseBody != null;
        assert responseBody.getName().equals(requestBody.getName());
        assert responseBody.getDescription().equals(requestBody.getDescription());
    }

    @Test
    public void testUpdateCategory() throws Exception {
        UUID categoryId = fakeCategories.getFirst().getId();
        CategoryRequest requestBody = new CategoryRequest("Updated Name", "Updated Description");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/product-categories/" + categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        CategoryResponse responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CategoryResponse.class
        );

        assert responseBody.getName().equals(requestBody.getName());
        assert responseBody.getDescription().equals(requestBody.getDescription());
    }

    @Test
    public void testDeleteCategory() throws Exception {
        UUID categoryId = fakeCategories.getFirst().getId();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/product-categories/" + categoryId);

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assert responseBody.equals("Category deleted Successfully.");
    }
}