package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.CartItem;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.carts.AddCartItemRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.carts.CartItemResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.carts.RemoveCartItemRequest;
import org.dti.se.finalproject1backend1.outers.exceptions.carts.CartItemNotFoundException;
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
public class CartRestTest extends TestConfiguration {

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
    public void testGetCartItems() throws Exception {
        List<CartItem> realCartItems = fakeCartItems
                .stream()
                .filter(cartItem -> cartItem.getAccount().getId().equals(authenticatedAccount.getId()))
                .toList();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/carts")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .param("page", "0")
                .param("size", "10");

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<List<CartItemResponse>> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Cart items found.");
        assert responseBody.getData() != null;
        assert responseBody.getData().size() == realCartItems.size();
        responseBody.getData().forEach(cartItemResponse -> {
            CartItem realCartItem = realCartItems
                    .stream()
                    .filter(cartItem -> cartItem.getId().equals(cartItemResponse.getId()))
                    .findFirst()
                    .orElse(null);
            assert realCartItem != null;
            assert cartItemResponse.getQuantity().equals(realCartItem.getQuantity());
            assert cartItemResponse.getProduct().getId().equals(realCartItem.getProduct().getId());
        });
    }

    @Test
    public void testAddCartItem() throws Exception {
        CartItem realCartItem = fakeCartItems
                .stream()
                .filter(cartItem -> cartItem.getAccount().getId().equals(authenticatedAccount.getId()))
                .findFirst()
                .orElse(null);

        assert realCartItem != null;
        AddCartItemRequest requestBody = AddCartItemRequest
                .builder()
                .productId(realCartItem.getProduct().getId())
                .quantity(1.0)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/carts/add")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<Void> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Item added to cart.");
        assert responseBody.getData() == null;

        CartItem foundCartItem = cartItemRepository
                .findByAccountIdAndProductId(authenticatedAccount.getId(), realCartItem.getProduct().getId())
                .orElseThrow(CartItemNotFoundException::new);

        assert foundCartItem != null;
        assert foundCartItem.getAccount().getId().equals(authenticatedAccount.getId());
        assert foundCartItem.getProduct().getId().equals(realCartItem.getProduct().getId());
        assert foundCartItem.getQuantity().equals(1.0);
    }

    @Test
    public void testRemoveCartItem() throws Exception {
        CartItem realCartItem = fakeCartItems
                .stream()
                .filter(cartItem -> cartItem.getAccount().getId().equals(authenticatedAccount.getId()))
                .findFirst()
                .orElse(null);

        assert realCartItem != null;
        RemoveCartItemRequest requestBody = RemoveCartItemRequest
                .builder()
                .productId(realCartItem.getProduct().getId())
                .quantity(realCartItem.getQuantity())
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/carts/remove")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<Void> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Item removed from cart.");
        assert responseBody.getData() == null;

        CartItem foundCartItem = cartItemRepository
                .findByAccountIdAndProductId(authenticatedAccount.getId(), realCartItem.getProduct().getId())
                .orElseThrow(CartItemNotFoundException::new);

        assert foundCartItem == null;
    }
}