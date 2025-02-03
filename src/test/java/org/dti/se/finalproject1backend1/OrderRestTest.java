package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderItemRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderProcessRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
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
    public void testCheckout() throws Exception {
        AccountAddress realAddress = fakeAccountAddresses
                .stream()
                .filter(accountAddress -> accountAddress.getAccount().getId().equals(authenticatedAccount.getId()))
                .findFirst()
                .orElseThrow();

        List<OrderItemRequest> orderItemRequests = fakeCartItems
                .stream()
                .filter(cartItem -> cartItem.getAccount().getId().equals(authenticatedAccount.getId()))
                .map(cartItem -> OrderItemRequest
                        .builder()
                        .productId(cartItem.getProduct().getId())
                        .quantity(cartItem.getQuantity())
                        .build()
                )
                .toList();

        OrderRequest requestBody = OrderRequest
                .builder()
                .addressId(realAddress.getId())
                .paymentMethod("AUTOMATIC")
                .items(orderItemRequests)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/orders/checkout")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

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

        assert responseBody.getMessage().equals("Order checked out.");
        assert responseBody.getData() != null;
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
    public void testGetPaymentConfirmations() throws Exception {
        List<Order> realOrders = fakeOrders
                .stream()
                .filter(order -> order
                        .getOrderStatuses()
                        .stream()
                        .anyMatch(orderStatus -> orderStatus.getStatus().equals("WAITING_FOR_PAYMENT_CONFIRMATION"))
                )
                .toList();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/orders/payment-confirmations")
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

        assert responseBody.getMessage().equals("Payment confirmation orders found.");
        assert responseBody.getData() != null;
        assert responseBody.getData().size() == realOrders.size();
    }


    @Test
    public void testApprovePaymentConfirmationOrder() throws Exception {
        Order realOrder = fakeOrders
                .stream()
                .filter(order -> {
                    List<OrderStatus> orderStatuses = fakeOrderStatuses
                            .stream()
                            .filter(orderStatus -> orderStatus.getOrder().getId().equals(order.getId()))
                            .sorted((a, b) -> a.getTime().compareTo(b.getTime()))
                            .toList();

                    return orderStatuses.getLast().getStatus().equals("WAITING_FOR_PAYMENT_CONFIRMATION");
                })
                .findFirst()
                .orElseThrow();

        OrderProcessRequest requestBody = OrderProcessRequest
                .builder()
                .orderId(realOrder.getId())
                .action("APPROVE")
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/orders/payment-confirmations/process")
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

        assert responseBody.getMessage().equals("Order payment confirmation processed.");
        assert responseBody.getData() == null;

        List<OrderStatus> updatedOrderStatuses = orderStatusRepository
                .findAllByOrderIdOrderByTimeAsc(realOrder.getId());

        assert updatedOrderStatuses.getLast().getStatus().equals("PROCESSING");
    }

    @Test
    public void testRejectPaymentConfirmationOrder() throws Exception {
        Order realOrder = fakeOrders
                .stream()
                .filter(order -> {
                    List<OrderStatus> orderStatuses = fakeOrderStatuses
                            .stream()
                            .filter(orderStatus -> orderStatus.getOrder().getId().equals(order.getId()))
                            .sorted((a, b) -> a.getTime().compareTo(b.getTime()))
                            .toList();

                    return orderStatuses.getLast().getStatus().equals("WAITING_FOR_PAYMENT_CONFIRMATION");
                })
                .findFirst()
                .orElseThrow();

        OrderProcessRequest requestBody = OrderProcessRequest
                .builder()
                .orderId(realOrder.getId())
                .action("REJECT")
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/orders/payment-confirmations/process")
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

        assert responseBody.getMessage().equals("Order payment confirmation processed.");
        assert responseBody.getData() == null;

        List<OrderStatus> updatedOrderStatuses = orderStatusRepository
                .findAllByOrderIdOrderByTimeAsc(realOrder.getId());

        assert updatedOrderStatuses.getLast().getStatus().equals("WAITING_FOR_PAYMENT");
    }

    @Test
    public void testCancelOrder() throws Exception {
        List<OrderStatus> shippingOrderStatuses = fakeOrderStatuses
                .stream()
                .filter(orderStatus -> orderStatus.getStatus().equals("SHIPPING"))
                .toList();

        Order realOrder = fakeOrders
                .stream()
                .filter(order -> shippingOrderStatuses
                        .stream()
                        .noneMatch(orderStatus -> orderStatus.getOrder().getId().equals(order.getId()))
                )
                .findFirst()
                .orElseThrow();

        OrderProcessRequest requestBody = OrderProcessRequest
                .builder()
                .orderId(realOrder.getId())
                .action("CANCEL")
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/orders/cancellations/process")
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

        assert responseBody.getMessage().equals("Order cancellation processed.");
        assert responseBody.getData() == null;

        List<OrderStatus> updatedOrderStatuses = orderStatusRepository
                .findAllByOrderIdOrderByTimeAsc(realOrder.getId());

        assert updatedOrderStatuses.getLast().getStatus().equals("CANCELED");
    }

}