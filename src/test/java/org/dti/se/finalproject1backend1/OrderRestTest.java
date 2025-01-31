package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Order;
import org.dti.se.finalproject1backend1.inners.models.entities.OrderItem;
import org.dti.se.finalproject1backend1.inners.models.entities.OrderStatus;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderRestTest extends TestConfiguration {

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
    public void testGetCustomerOrders() throws Exception {
        List<Order> realOrders = fakeOrders
                .stream()
                .filter(cartItem -> cartItem.getAccount().getId().equals(authenticatedAccount.getId()))
                .toList();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/orders/customer")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .param("page", "0")
                .param("size", String.valueOf(realOrders.size()));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<List<OrderResponse>> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Orders found.");
        assert responseBody.getData() != null;
        assert responseBody.getData().size() == realOrders.size();
        responseBody.getData().forEach(orderResponse -> {
            Optional<Order> realOrder = fakeOrders
                    .stream()
                    .filter(order -> order.getId().equals(orderResponse.getId()))
                    .findFirst();

            assert realOrder.isPresent();
            assert orderResponse.getTotalPrice().equals(realOrder.get().getTotalPrice());
            assert orderResponse.getShipmentOrigin().equals(realOrder.get().getShipmentOrigin());
            assert orderResponse.getShipmentDestination().equals(realOrder.get().getShipmentDestination());
            assert orderResponse.getShipmentPrice().equals(realOrder.get().getShipmentPrice());
            assert orderResponse.getItemPrice().equals(realOrder.get().getItemPrice());

            List<OrderStatus> realOrderStatuses = fakeOrderStatuses
                    .stream()
                    .filter(orderStatus -> orderStatus.getOrder().getId().equals(orderResponse.getId()))
                    .toList();

            assert orderResponse.getStatuses().size() == realOrderStatuses.size();
            orderResponse.getStatuses().forEach(orderStatusResponse -> {
                Optional<OrderStatus> realOrderStatus = fakeOrderStatuses
                        .stream()
                        .filter(orderStatus -> orderStatus.getId().equals(orderStatusResponse.getId()))
                        .findFirst();

                assert realOrderStatus.isPresent();
                assert orderStatusResponse.getStatus().equals(realOrderStatus.get().getStatus());
                assert orderStatusResponse.getTime().isEqual(realOrderStatus.get().getTime());
            });

            List<OrderItem> realOrderItems = fakeOrderItems
                    .stream()
                    .filter(orderItem -> orderItem.getOrder().getId().equals(orderResponse.getId()))
                    .toList();

            assert orderResponse.getItems().size() == realOrderItems.size();
            orderResponse.getItems().forEach(orderItemResponse -> {
                Optional<OrderItem> realOrderItem = fakeOrderItems
                        .stream()
                        .filter(orderItem -> orderItem.getId().equals(orderItemResponse.getId()))
                        .findFirst();

                assert realOrderItem.isPresent();
                assert orderItemResponse.getQuantity().equals(realOrderItem.get().getQuantity());
                assert orderItemResponse.getProduct().getId().equals(realOrderItem.get().getProduct().getId());
            });
        });
    }


    @Test
    public void testGetOrders() throws Exception {
        List<Order> realOrders = fakeOrders;

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/orders")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .param("page", "0")
                .param("size", String.valueOf(realOrders.size()));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<List<OrderResponse>> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Orders found.");
        assert responseBody.getData() != null;
        assert responseBody.getData().size() == realOrders.size();
    }


    @Test
    public void testGetOrder() throws Exception {
        Order realOrders = fakeOrders.getFirst();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/orders/{orderId}", realOrders.getId())
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken());

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<OrderResponse> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Order found.");
        assert responseBody.getData() != null;

        assert responseBody.getData().getId().equals(realOrders.getId());
        assert responseBody.getData().getTotalPrice().equals(realOrders.getTotalPrice());
        assert responseBody.getData().getShipmentOrigin().equals(realOrders.getShipmentOrigin());
        assert responseBody.getData().getShipmentDestination().equals(realOrders.getShipmentDestination());
        assert responseBody.getData().getShipmentPrice().equals(realOrders.getShipmentPrice());
        assert responseBody.getData().getItemPrice().equals(realOrders.getItemPrice());

        List<OrderStatus> realOrderStatuses = fakeOrderStatuses
                .stream()
                .filter(orderStatus -> orderStatus.getOrder().getId().equals(responseBody.getData().getId()))
                .toList();

        assert responseBody.getData().getStatuses().size() == realOrderStatuses.size();

        responseBody.getData().getStatuses().forEach(orderStatusResponse -> {
            Optional<OrderStatus> realOrderStatus = fakeOrderStatuses
                    .stream()
                    .filter(orderStatus -> orderStatus.getId().equals(orderStatusResponse.getId()))
                    .findFirst();

            assert realOrderStatus.isPresent();
            assert orderStatusResponse.getStatus().equals(realOrderStatus.get().getStatus());
            assert orderStatusResponse.getTime().isEqual(realOrderStatus.get().getTime());
        });

        List<OrderItem> realOrderItems = fakeOrderItems
                .stream()
                .filter(orderItem -> orderItem.getOrder().getId().equals(realOrders.getId()))
                .toList();

        assert responseBody.getData().getItems().size() == realOrderItems.size();

        responseBody.getData().getItems().forEach(orderItemResponse -> {
            Optional<OrderItem> realOrderItem = fakeOrderItems
                    .stream()
                    .filter(orderItem -> orderItem.getId().equals(orderItemResponse.getId()))
                    .findFirst();

            assert realOrderItem.isPresent();
            assert orderItemResponse.getQuantity().equals(realOrderItem.get().getQuantity());
            assert orderItemResponse.getProduct().getId().equals(realOrderItem.get().getProduct().getId());
        });
    }
}