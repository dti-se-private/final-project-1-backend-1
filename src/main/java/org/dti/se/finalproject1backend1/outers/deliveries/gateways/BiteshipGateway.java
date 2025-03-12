package org.dti.se.finalproject1backend1.outers.deliveries.gateways;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.CartItem;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.shipments.ShipmentRateResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.orders.ShipmentInvalidException;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BiteshipGateway {

    @Autowired
    Environment environment;

    @Autowired
    ObjectMapper objectMapper;

    public ShipmentRateResponse getShipmentRate(Point origin, Point destination, List<CartItem> items) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", environment.getProperty("biteship.api.key"));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("origin_longitude", origin.getX());
        requestBody.put("origin_latitude", origin.getY());
        requestBody.put("destination_longitude", destination.getX());
        requestBody.put("destination_latitude", destination.getY());
        requestBody.put("couriers", "pos");

        ArrayList<Map<String, Object>> itemsList = new ArrayList<>();
        for (CartItem item : items) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("value", item.getProduct().getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("weight", item.getProduct().getWeight());
            itemsList.add(itemMap);
        }

        requestBody.put("items", itemsList);

        try {
            String json = objectMapper.writeValueAsString(requestBody);
            System.out.println("BiteshipGateway.getShipmentRate: Request body: " + json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String url = environment.getProperty("biteship.api.url") + "/v1/rates/couriers";
            ResponseEntity<ShipmentRateResponse> response = restTemplate
                    .exchange(url, HttpMethod.POST, request, ShipmentRateResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException httpException) {
            throw new ShipmentInvalidException();
        }
    }
}