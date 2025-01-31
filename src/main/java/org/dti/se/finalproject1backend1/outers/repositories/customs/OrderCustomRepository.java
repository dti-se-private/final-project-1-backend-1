package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OrderCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public List<OrderResponse> getCustomerOrders(
            Account account,
            Integer page,
            Integer size,
            List<String> filters,
            String search
    ) {
        String order = filters
                .stream()
                .map(filter -> String.format("SIMILARITY(%s::text, '%s')", filter, search))
                .collect(Collectors.joining("+"));

        if (order.isEmpty()) {
            order = "\"order\".id";
        }

        String query = String.format("""
                        SELECT json_build_object(
                            'id', "order".id,
                            'totalPrice', "order".total_price,
                            'shipmentOrigin', "order".shipment_origin,
                            'shipmentDestination', "order".shipment_destination,
                            'shipmentPrice', "order".shipment_price,
                            'itemPrice', "order".item_price,
                            'statuses', (
                                SELECT json_agg(json_build_object(
                                    'id', order_status.id,
                                    'status', order_status.status,
                                    'time', order_status.time
                                ))
                                FROM (
                                    SELECT *
                                    FROM order_status
                                    WHERE order_status.order_id = "order".id
                                    ORDER BY order_status.time
                                ) as order_status
                            ),
                            'items', (
                                SELECT json_agg(json_build_object(
                                    'id', order_item.id,
                                    'quantity', order_item.quantity,
                                    'product', json_build_object(
                                        'id', product.id,
                                        'name', product.name,
                                        'description', product.description,
                                        'price', product.price,
                                        'image', product.image,
                                        'category', json_build_object(
                                            'id', category.id,
                                            'name', category.name,
                                            'description', category.description
                                        )
                                    )
                                ))
                                FROM order_item
                                JOIN product ON order_item.product_id = product.id
                                JOIN category ON product.category_id = category.id
                                WHERE order_item.order_id = "order".id
                            )
                        ) as item
                FROM "order"
                WHERE "order".account_id = ?
                ORDER BY %s
                LIMIT ?
                OFFSET ?
                """, order);

        return oneTemplate
                .query(query,
                        (rs, rowNum) -> {
                            try {
                                return objectMapper.readValue(rs.getString("item"), new TypeReference<>() {
                                });
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        account.getId(),
                        size,
                        page * size
                );
    }

    public List<OrderResponse> getOrders(Integer page, Integer size, List<String> filters, String search) {
        String order = filters
                .stream()
                .map(filter -> String.format("SIMILARITY(%s::text, '%s')", filter, search))
                .collect(Collectors.joining("+"));

        if (order.isEmpty()) {
            order = "\"order\".id";
        }

        String query = String.format("""
                        SELECT json_build_object(
                            'id', "order".id,
                            'totalPrice', "order".total_price,
                            'shipmentOrigin', "order".shipment_origin,
                            'shipmentDestination', "order".shipment_destination,
                            'shipmentPrice', "order".shipment_price,
                            'itemPrice', "order".item_price,
                            'statuses', (
                                SELECT json_agg(json_build_object(
                                    'id', order_status.id,
                                    'status', order_status.status,
                                    'time', order_status.time
                                ))
                                FROM (
                                    SELECT *
                                    FROM order_status
                                    WHERE order_status.order_id = "order".id
                                    ORDER BY order_status.time
                                ) as order_status
                            ),
                            'items', (
                                SELECT json_agg(json_build_object(
                                    'id', order_item.id,
                                    'quantity', order_item.quantity,
                                    'product', json_build_object(
                                        'id', product.id,
                                        'name', product.name,
                                        'description', product.description,
                                        'price', product.price,
                                        'image', product.image,
                                        'category', json_build_object(
                                            'id', category.id,
                                            'name', category.name,
                                            'description', category.description
                                        )
                                    )
                                ))
                                FROM order_item
                                JOIN product ON order_item.product_id = product.id
                                JOIN category ON product.category_id = category.id
                                WHERE order_item.order_id = "order".id
                            )
                        ) as item
                FROM "order"
                ORDER BY %s
                LIMIT ?
                OFFSET ?
                """, order);

        return oneTemplate
                .query(query,
                        (rs, rowNum) -> {
                            try {
                                return objectMapper.readValue(rs.getString("item"), new TypeReference<>() {
                                });
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        size,
                        page * size
                );
    }
}