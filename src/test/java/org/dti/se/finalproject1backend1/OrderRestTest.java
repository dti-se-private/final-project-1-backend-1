package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountAddress;
import org.dti.se.finalproject1backend1.inners.models.entities.Order;
import org.dti.se.finalproject1backend1.inners.models.entities.OrderStatus;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.*;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.LocationCustomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Comparator;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderRestTest extends TestConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    protected LocationCustomRepository locationCustomRepository;

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
    @ResourceLock("mockLocationCustomRepository")
    public void testCheckout() throws Exception {
        Mockito.when(locationCustomRepository.getNearestWarehouse(Mockito.any()))
                .thenReturn(fakeWarehouses.getFirst());

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
    @ResourceLock("mockLocationCustomRepository")
    public void testAutomaticPayment() throws Exception {
        Mockito.when(locationCustomRepository.getNearestExistingWarehouseProduct(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(fakeWarehouseProducts.getFirst());

        Order realOrder = fakeOrders
                .stream()
                .filter(order -> order
                        .getOrderStatuses()
                        .stream()
                        .noneMatch(orderStatus -> orderStatus.getStatus().equals("PROCESSING") || orderStatus.getStatus().equals("WAITING_FOR_PAYMENT_CONFIRMATION"))
                )
                .findFirst()
                .orElseThrow(OrderNotFoundException::new);

        PaymentProcessRequest requestBody = PaymentProcessRequest
                .builder()
                .orderId(realOrder.getId())
                .paymentMethod("AUTOMATIC")
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/orders/payments/process")
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

        assert responseBody.getMessage().equals("Order payment processed.");
        assert responseBody.getData() != null;
        assert responseBody.getData().getStatuses().getLast().getStatus().equals("SHIPPING");
    }

    @Test
    @ResourceLock("mockLocationCustomRepository")
    public void testManualPayment() throws Exception {
        Mockito.when(locationCustomRepository.getNearestExistingWarehouseProduct(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(fakeWarehouseProducts.getFirst());

        Order realOrder = fakeOrders
                .stream()
                .filter(order -> order
                        .getOrderStatuses()
                        .stream()
                        .noneMatch(orderStatus -> orderStatus.getStatus().equals("PROCESSING") || orderStatus.getStatus().equals("WAITING_FOR_PAYMENT_CONFIRMATION"))
                )
                .findFirst()
                .orElseThrow(OrderNotFoundException::new);

        PaymentProcessRequest requestBody = PaymentProcessRequest
                .builder()
                .orderId(realOrder.getId())
                .paymentMethod("MANUAL")
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/orders/payments/process")
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

        assert responseBody.getMessage().equals("Order payment processed.");
        assert responseBody.getData() != null;
        assert responseBody.getData().getStatuses().getLast().getStatus().equals("WAITING_FOR_PAYMENT_CONFIRMATION");
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
    @ResourceLock("mockLocationCustomRepository")
    public void testApprovePaymentConfirmationOrder() throws Exception {
        Mockito.when(locationCustomRepository.getNearestExistingWarehouseProduct(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(fakeWarehouseProducts.getFirst());

        Order realOrder = fakeOrders
                .stream()
                .filter(order -> {
                    List<OrderStatus> orderStatuses = fakeOrderStatuses
                            .stream()
                            .filter(orderStatus -> orderStatus.getOrder().getId().equals(order.getId()))
                            .sorted(Comparator.comparing(OrderStatus::getTime))
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

        ResponseBody<OrderResponse> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Order payment confirmation processed.");
        assert responseBody.getData() != null;
        assert responseBody.getData().getStatuses().getLast().getStatus().equals("SHIPPING");
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

        ResponseBody<OrderResponse> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Order payment confirmation processed.");
        assert responseBody.getData() != null;
        assert responseBody.getData().getStatuses().getLast().getStatus().equals("WAITING_FOR_PAYMENT");
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

        ResponseBody<OrderResponse> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Order cancellation processed.");
        assert responseBody.getData() != null;
        assert responseBody.getData().getStatuses().getLast().getStatus().equals("CANCELED");
    }

}