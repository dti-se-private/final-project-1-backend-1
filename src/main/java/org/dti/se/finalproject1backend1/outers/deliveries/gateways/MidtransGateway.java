package org.dti.se.finalproject1backend1.outers.deliveries.gateways;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Order;
import org.dti.se.finalproject1backend1.inners.models.entities.OrderItem;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.payments.CreatePaymentLinkResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.payments.GetPaymentLinkResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.PaymentLinkInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class MidtransGateway {

    @Autowired
    Environment environment;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RestTemplate restTemplate;

    public CreatePaymentLinkResponse getPaymentLinkUrl(Order order, Double grossAmount) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String apiKey = Objects.requireNonNull(environment.getProperty("midtrans.api.key"));
        String base64ApiKey = Base64.getEncoder().withoutPadding().encodeToString(apiKey.getBytes());
        headers.set("authorization", "Basic " + base64ApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", order.getId());
        transactionDetails.put("gross_amount", grossAmount);
        requestBody.put("transaction_details", transactionDetails);

        List<Map<String, Object>> itemDetails = new ArrayList<>();

        Map<String, Object> shipmentDetail = new HashMap<>();
        shipmentDetail.put("name", "Shipment Cost");
        shipmentDetail.put("price", order.getShipmentPrice());
        shipmentDetail.put("quantity", 1);
        itemDetails.add(shipmentDetail);

        for (OrderItem item : order.getOrderItems()) {
            Map<String, Object> itemDetail = new HashMap<>();
            itemDetail.put("id", item.getId());
            itemDetail.put("name", item.getProduct().getName());
            itemDetail.put("price", item.getProduct().getPrice());
            itemDetail.put("quantity", item.getQuantity());
            itemDetails.add(itemDetail);
        }
        requestBody.put("item_details", itemDetails);

        Map<String, Object> customerDetails = new HashMap<>();
        customerDetails.put("first_name", order.getAccount().getName());
        customerDetails.put("email", order.getAccount().getEmail());
        String phone = order.getAccount().getPhone();
        if (!(phone.length() >= 5 && phone.length() <= 20)) {
            phone = "00000";
        }
        customerDetails.put("phone", phone);
        requestBody.put("customer_details", customerDetails);

        requestBody.put("customer_required", false);
        requestBody.put("usage_limit", 1);

        Map<String, Object> callbacks = new HashMap<>();
        String finishUrl = environment.getProperty("midtrans.callback.host") + "/customers/orders/" + order.getId();
        callbacks.put("finish", finishUrl);
        requestBody.put("callbacks", callbacks);

        try {
            String json = objectMapper.writeValueAsString(requestBody);
            System.out.println("MidtransGateway.getPaymentLinkUrl: Request body: " + json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        try {
            String createUrl = environment.getProperty("midtrans.api.url") + "/v1/payment-links";
            HttpEntity<Map<String, Object>> createRequest = new HttpEntity<>(requestBody, headers);
            ResponseEntity<CreatePaymentLinkResponse> createResponse = restTemplate
                    .exchange(createUrl, HttpMethod.POST, createRequest, CreatePaymentLinkResponse.class);
            return createResponse.getBody();
        } catch (HttpClientErrorException.Conflict conflictException) {
            try {
                String getUrl = environment.getProperty("midtrans.api.url") + "/v1/payment-links/" + order.getId();
                HttpEntity<Void> getRequest = new HttpEntity<>(headers);
                ResponseEntity<GetPaymentLinkResponse> getResponse = restTemplate
                        .exchange(getUrl, HttpMethod.GET, getRequest, GetPaymentLinkResponse.class);
                return CreatePaymentLinkResponse
                        .builder()
                        .orderId(Objects.requireNonNull(getResponse.getBody()).getOrderId())
                        .paymentUrl(Objects.requireNonNull(getResponse.getBody()).getPaymentLinkUrl())
                        .build();
            } catch (HttpClientErrorException.NotFound notFoundException) {
                throw new PaymentLinkInvalidException();
            }
        }
    }
}