package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountAddress;
import org.dti.se.finalproject1backend1.inners.models.entities.Order;
import org.dti.se.finalproject1backend1.inners.models.entities.OrderStatus;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.payments.CreatePaymentLinkResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.shipments.ShipmentPricingResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.shipments.ShipmentRateResponse;
import org.dti.se.finalproject1backend1.outers.deliveries.gateways.BiteshipGateway;
import org.dti.se.finalproject1backend1.outers.deliveries.gateways.MidtransGateway;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.OrderStatusNotFoundException;
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

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderRestTest extends TestConfiguration {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    protected LocationCustomRepository locationCustomRepository;

    @MockitoBean
    protected BiteshipGateway biteshipGateway;

    @MockitoBean
    protected MidtransGateway midtransGateway;

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
    @ResourceLock("locationCustomRepositoryMock")
    public void testCheckout() throws Exception {
        Mockito.when(locationCustomRepository.getNearestWarehouse(Mockito.any()))
                .thenReturn(fakeWarehouses.getFirst());

        ShipmentRateResponse shipmentRate = ShipmentRateResponse
                .builder()
                .pricing(List.of(
                        ShipmentPricingResponse
                                .builder()
                                .price(123456.7)
                                .build()
                ))
                .build();
        Mockito.when(biteshipGateway.getShipmentRate(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(shipmentRate);

        AccountAddress realAddress = fakeAccountAddresses
                .stream()
                .filter(accountAddress -> accountAddress.getAccount().getId().equals(authenticatedAccount.getId()))
                .findFirst()
                .orElseThrow();

        OrderRequest requestBody = OrderRequest
                .builder()
                .addressId(realAddress.getId())
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/orders/checkout")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
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
    @ResourceLock("locationCustomRepositoryMock")
    public void testAutomaticPayment() throws Exception {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        Mockito.when(locationCustomRepository.getNearestExistingWarehouseProduct(Mockito.any(), Mockito.any()))
                .thenReturn(fakeWarehouseProducts.getFirst());

        Order realOrder = fakeOrders
                .stream()
                .filter(order -> fakeOrderStatuses
                        .stream()
                        .filter(orderStatus -> orderStatus.getOrder().getId().equals(order.getId()))
                        .noneMatch(orderStatus -> orderStatus.getStatus().equals("WAITING_FOR_PAYMENT_CONFIRMATION"))
                )
                .findFirst()
                .orElseThrow(OrderNotFoundException::new);

        AutomaticPaymentProcessRequest requestBody = AutomaticPaymentProcessRequest
                .builder()
                .orderId(String.format("%s-%s", realOrder.getId(), now.toInstant().toEpochMilli()))
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/orders/automatic-payments/process")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<OrderResponse> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Order automatic payment processed.");
        assert responseBody.getData() != null;
        assert responseBody.getData().getStatuses().getLast().getStatus().equals("PROCESSING");
    }

    @Test
    @ResourceLock("locationCustomRepositoryMock")
    public void testManualPayment() throws Exception {
        Mockito.when(locationCustomRepository.getNearestExistingWarehouseProduct(Mockito.any(), Mockito.any()))
                .thenReturn(fakeWarehouseProducts.getFirst());

        Order realOrder = fakeOrders
                .stream()
                .filter(order -> fakeOrderStatuses
                        .stream()
                        .filter(orderStatus -> orderStatus.getOrder().getId().equals(order.getId()))
                        .max(Comparator.comparing(OrderStatus::getTime))
                        .orElseThrow(OrderStatusNotFoundException::new)
                        .getStatus()
                        .equals("WAITING_FOR_PAYMENT")
                )
                .findFirst()
                .orElseThrow(OrderNotFoundException::new);

        PaymentProofRequest paymentProofRequest = PaymentProofRequest
                .builder()
                .file("file".getBytes())
                .extension("extension")
                .build();

        ManualPaymentProcessRequest requestBody = ManualPaymentProcessRequest
                .builder()
                .orderId(realOrder.getId())
                .paymentProofs(List.of(paymentProofRequest))
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/orders/manual-payments/process")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<OrderResponse> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Order manual payment processed.");
        assert responseBody.getData() != null;
        assert responseBody.getData().getStatuses().getLast().getStatus().equals("WAITING_FOR_PAYMENT_CONFIRMATION");
    }

    @Test
    public void testGetOrders() throws Exception {
        List<Order> realOrders = fakeOrders;

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/orders")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .param("page", "0")
                .param("size", String.valueOf(realOrders.size()));

        MvcResult result = mockMvc
                .perform(httpRequest)
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
    public void testGetPaymentConfirmationOrders() throws Exception {
        List<Order> realOrders = fakeOrders
                .stream()
                .filter(order -> order
                        .getOrderStatuses()
                        .stream()
                        .anyMatch(orderStatus -> orderStatus.getStatus().equals("WAITING_FOR_PAYMENT_CONFIRMATION"))
                )
                .toList();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/orders/payment-confirmations")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .param("page", "0")
                .param("size", String.valueOf(realOrders.size()));

        MvcResult result = mockMvc
                .perform(httpRequest)
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
    @ResourceLock("locationCustomRepositoryMock")
    public void testApprovePaymentConfirmationOrder() throws Exception {
        Mockito.when(locationCustomRepository.getNearestExistingWarehouseProduct(Mockito.any(), Mockito.any()))
                .thenReturn(fakeWarehouseProducts.getFirst());

        Order realOrder = fakeOrders
                .stream()
                .filter(order -> fakeOrderStatuses
                        .stream()
                        .filter(orderStatus -> orderStatus.getOrder().getId().equals(order.getId()))
                        .max(Comparator.comparing(OrderStatus::getTime))
                        .orElseThrow(OrderStatusNotFoundException::new)
                        .getStatus()
                        .equals("WAITING_FOR_PAYMENT_CONFIRMATION")
                )
                .findFirst()
                .orElseThrow(OrderNotFoundException::new);

        OrderProcessRequest requestBody = OrderProcessRequest
                .builder()
                .orderId(realOrder.getId())
                .action("APPROVE")
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/orders/payment-confirmations/process")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
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
        assert responseBody.getData().getStatuses().getLast().getStatus().equals("PROCESSING");
    }

    @Test
    public void testRejectPaymentConfirmationOrder() throws Exception {
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
                .action("REJECT")
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/orders/payment-confirmations/process")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
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
    public void testGetShipmentStartConfirmations() throws Exception {
        List<Order> realOrders = fakeOrders
                .stream()
                .filter(order -> order
                        .getOrderStatuses()
                        .stream()
                        .anyMatch(orderStatus -> orderStatus.getStatus().equals("PROCESSING"))
                )
                .toList();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .get("/orders/shipment-start-confirmations")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .param("page", "0")
                .param("size", String.valueOf(realOrders.size()));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<List<OrderResponse>> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Shipment start confirmation orders found.");
        assert responseBody.getData() != null;
        assert responseBody.getData().size() == realOrders.size();
    }

    @Test
    public void testProcessShipmentStartConfirmationConfirmationOrder() throws Exception {
        Order realOrder = fakeOrders
                .stream()
                .filter(order -> {
                    List<OrderStatus> orderStatuses = fakeOrderStatuses
                            .stream()
                            .filter(orderStatus -> orderStatus.getOrder().getId().equals(order.getId()))
                            .sorted(Comparator.comparing(OrderStatus::getTime))
                            .toList();

                    return orderStatuses.getLast().getStatus().equals("PROCESSING");
                })
                .findFirst()
                .orElseThrow();

        OrderProcessRequest requestBody = OrderProcessRequest
                .builder()
                .orderId(realOrder.getId())
                .action("APPROVE")
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/orders/shipment-start-confirmations/process")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<OrderResponse> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Order shipment start confirmation processed.");
        assert responseBody.getData() != null;
        assert responseBody.getData().getStatuses().getLast().getStatus().equals("SHIPPING");
    }


    @Test
    public void testProcessShipmentConfirmationOrder() throws Exception {
        Order realOrder = fakeOrders
                .stream()
                .filter(order -> {
                    List<OrderStatus> orderStatuses = fakeOrderStatuses
                            .stream()
                            .filter(orderStatus -> orderStatus.getOrder().getId().equals(order.getId()))
                            .sorted(Comparator.comparing(OrderStatus::getTime))
                            .toList();

                    return orderStatuses.getLast().getStatus().equals("SHIPPING");
                })
                .findFirst()
                .orElseThrow();

        OrderProcessRequest requestBody = OrderProcessRequest
                .builder()
                .orderId(realOrder.getId())
                .action("APPROVE")
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/orders/shipment-confirmations/process")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<OrderResponse> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Order shipment confirmation processed.");
        assert responseBody.getData() != null;
        assert responseBody.getData().getStatuses().getLast().getStatus().equals("ORDER_CONFIRMED");
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

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/orders/cancellations/process")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
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


    @Test
    public void testProcessPaymentGateway() throws Exception {
        Order realOrder = fakeOrders
                .stream()
                .filter(order -> fakeOrderStatuses
                        .stream()
                        .filter(orderStatus -> orderStatus.getOrder().getId().equals(order.getId()))
                        .noneMatch(orderStatus -> orderStatus.getStatus().equals("WAITING_FOR_PAYMENT_CONFIRMATION"))
                )
                .findFirst()
                .orElseThrow(OrderNotFoundException::new);

        Mockito.when(midtransGateway.getPaymentLinkUrl(Mockito.any(), Mockito.any()))
                .thenReturn(CreatePaymentLinkResponse
                        .builder()
                        .orderId(realOrder.getId())
                        .paymentUrl("https://example.com")
                        .build()
                );

        PaymentGatewayRequest requestBody = PaymentGatewayRequest
                .builder()
                .orderId(realOrder.getId())
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/orders/payment-gateways/process")
                .header("Authorization", "Bearer " + authenticatedSession.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        ResponseBody<PaymentGatewayResponse> responseBody = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assert responseBody.getMessage().equals("Order payment gateway processed.");
        assert responseBody.getData() != null;
        assert responseBody.getData().getUrl() != null;
    }

}