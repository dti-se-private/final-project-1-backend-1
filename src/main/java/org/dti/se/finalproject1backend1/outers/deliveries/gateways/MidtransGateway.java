package org.dti.se.finalproject1backend1.outers.deliveries.gateways;

import org.dti.se.finalproject1backend1.inners.models.entities.OrderItem;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.payments.PaymentLinkResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class MidtransGateway {

    @Autowired
    Environment environment;

    public PaymentLinkResponse getPaymentLinkUrl(UUID orderId, Double grossAmount, List<OrderItem> items) {
        RestTemplate restTemplate = new RestTemplate();
        String url = environment.getProperty("midtrans.api.url") + "/v1/payment-links";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Basic " + environment.getProperty("midtrans.api.key"));

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", orderId);
        transactionDetails.put("gross_amount", grossAmount);
        requestBody.put("transaction_details", transactionDetails);

        List<Map<String, Object>> itemDetails = new ArrayList<>();
        for (OrderItem item : items) {
            Map<String, Object> itemDetail = new HashMap<>();
            itemDetail.put("id", item.getId());
            itemDetail.put("name", item.getProduct().getName());
            itemDetail.put("price", item.getProduct().getPrice());
            itemDetail.put("quantity", item.getQuantity());
            itemDetails.add(itemDetail);
        }
        requestBody.put("item_details", itemDetails);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForObject(url, request, PaymentLinkResponse.class);
    }
}