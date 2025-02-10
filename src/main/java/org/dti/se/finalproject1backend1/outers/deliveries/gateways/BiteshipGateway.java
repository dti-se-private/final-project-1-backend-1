package org.dti.se.finalproject1backend1.outers.deliveries.gateways;

import org.dti.se.finalproject1backend1.inners.models.entities.CartItem;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.shipments.ShipmentRateResponse;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BiteshipGateway {

    @Autowired
    Environment environment;

    public ShipmentRateResponse getShipmentRate(Point origin, Point destination, List<CartItem> items) {
        RestTemplate restTemplate = new RestTemplate();
        String url = environment.getProperty("biteship.api.url") + "/v1/rates/couriers";

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
            itemMap.put("weight", 1000);
            itemsList.add(itemMap);
        }

        requestBody.put("items", itemsList);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForObject(url, request, ShipmentRateResponse.class);
    }
}