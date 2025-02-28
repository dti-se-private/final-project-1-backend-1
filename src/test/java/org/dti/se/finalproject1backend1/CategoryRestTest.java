package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.products.CategoryNotFoundException;
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
    public void testGetCategories() throws Exception {
        List<Category> realCategories = fakeCategories;

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/categories")
                .param("page", "0")
                .param("size", String.valueOf(realCategories.size()))
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<List<CategoryResponse>> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Categories found.");
        assert responseBody.getData().size() == realCategories.size();
    }

    @Test
    public void testGetCategory() throws Exception {
        Category realCategory = fakeCategories
                .stream()
                .findFirst()
                .orElseThrow(CategoryNotFoundException::new);

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/categories/{categoryId}", realCategory.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<CategoryResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Category found.");
        assert responseBody.getData().getId().equals(realCategory.getId());
        assert responseBody.getData().getName().equals(realCategory.getName());
        assert responseBody.getData().getDescription().equals(realCategory.getDescription());
    }

    @Test
    public void testAddCategory() throws Exception {
        CategoryRequest requestBody = CategoryRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .description(String.format("description-%s", UUID.randomUUID()))
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isCreated())
                .andReturn();

        ResponseBody<CategoryResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Category added.");
        assert responseBody.getData().getId() != null;
        assert responseBody.getData().getName().equals(requestBody.getName());
        assert responseBody.getData().getDescription().equals(requestBody.getDescription());

        Category savedCategory = categoryRepository
                .findById(responseBody.getData().getId())
                .orElseThrow(CategoryNotFoundException::new);
        fakeCategories.add(savedCategory);
    }

    @Test
    public void testPatchCategory() throws Exception {
        Category realCategory = fakeCategories
                .stream()
                .findFirst()
                .orElseThrow(CategoryNotFoundException::new);

        CategoryRequest requestBody = CategoryRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .description(String.format("description-%s", UUID.randomUUID()))
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .patch("/categories/{categoryId}", realCategory.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<CategoryResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Category patched.");
        assert responseBody.getData().getId().equals(realCategory.getId());
        assert responseBody.getData().getName().equals(requestBody.getName());
        assert responseBody.getData().getDescription().equals(requestBody.getDescription());

        Category patchedCategory = categoryRepository
                .findById(responseBody.getData().getId())
                .orElseThrow(CategoryNotFoundException::new);

        fakeCategories.set(fakeCategories.indexOf(realCategory), patchedCategory);
    }

    @Test
    public void testDeleteCategory() throws Exception {
        Category realCategory = fakeCategories
                .stream()
                .findFirst()
                .orElseThrow(CategoryNotFoundException::new);

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .delete("/categories/{categoryId}", realCategory.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<CategoryResponse> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assert responseBody != null;
        assert responseBody.getMessage().equals("Category deleted.");
        assert categoryRepository.findById(realCategory.getId()).isEmpty();
        fakeCategories.remove(realCategory);
    }
}