package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

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
            String search
    ) {
        String sql = """
                SELECT *
                FROM (
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
                        ),
                         'paymentProofs', (
                             SELECT json_agg(json_build_object(
                                 'id', payment_proof.id,
                                 'file', payment_proof.file,
                                 'extension', payment_proof.extension,
                                 'time', payment_proof.time
                             ))
                             FROM payment_proof
                             WHERE payment_proof.order_id = "order".id
                         )
                    ) as item
                    FROM "order"
                    WHERE "order".account_id = ?
                    LIMIT ?
                    OFFSET ?
                ) as sq1
                ORDER BY SIMILARITY(sq1.item::text, ?) DESC
                """;

        return oneTemplate
                .query(sql,
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
                        page * size,
                        search
                );
    }

    public List<OrderResponse> getOrders(Account account, Integer page, Integer size, String search) {
        String sql = """
                SELECT *
                FROM (
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
                        ),
                        'paymentProofs', (
                            SELECT json_agg(json_build_object(
                                'id', payment_proof.id,
                                'file', payment_proof.file,
                                'extension', payment_proof.extension,
                                'time', payment_proof.time
                            ))
                            FROM payment_proof
                            WHERE payment_proof.order_id = "order".id
                        )
                    ) as item
                    FROM "order"
                    WHERE "order".id in (
                        SELECT DISTINCT order_item.order_id
                        FROM order_item
                        JOIN warehouse_product ON order_item.product_id = warehouse_product.product_id
                        JOIN warehouse_admin ON warehouse_product.warehouse_id = warehouse_admin.warehouse_id
                        WHERE order_item.order_id = "order".id
                        AND warehouse_admin.account_id = ?
                    )
                    LIMIT ?
                    OFFSET ?
                ) as sq1
                ORDER BY SIMILARITY(sq1.item::text, ?) DESC
                """;

        return oneTemplate
                .query(sql,
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
                        page * size,
                        search
                );
    }

    public List<OrderResponse> getOrders(Integer page, Integer size, String search) {
        String sql = """
                SELECT *
                FROM (
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
                                ),
                                'paymentProofs', (
                                    SELECT json_agg(json_build_object(
                                        'id', payment_proof.id,
                                        'file', payment_proof.file,
                                        'extension', payment_proof.extension,
                                        'time', payment_proof.time
                                    ))
                                    FROM payment_proof
                                    WHERE payment_proof.order_id = "order".id
                                )
                            ) as item
                    FROM "order"
                    LIMIT ?
                    OFFSET ?
                ) as sq1
                ORDER BY SIMILARITY(sq1.item::text, ?) DESC
                """;

        return oneTemplate
                .query(sql,
                        (rs, rowNum) -> {
                            try {
                                return objectMapper.readValue(rs.getString("item"), new TypeReference<>() {
                                });
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        size,
                        page * size,
                        search
                );
    }

    public OrderResponse getOrder(UUID orderId) {
        String sql = """
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
                    ),
                    'paymentProofs', (
                        SELECT json_agg(json_build_object(
                            'id', payment_proof.id,
                            'file', payment_proof.file,
                            'extension', payment_proof.extension,
                            'time', payment_proof.time
                        ))
                        FROM payment_proof
                        WHERE payment_proof.order_id = "order".id
                    )
                ) as item
                FROM "order"
                WHERE "order".id = ?
                LIMIT 1
                """;

        try {
            return oneTemplate
                    .queryForObject(sql,
                            (rs, rowNum) -> {
                                try {
                                    return objectMapper.readValue(rs.getString("item"), new TypeReference<>() {
                                    });
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            orderId
                    );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<OrderResponse> getPaymentConfirmationOrders(Account account, Integer page, Integer size, String search) {
        String sql = """
                SELECT *
                FROM (
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
                                ),
                                'paymentProofs', (
                                    SELECT json_agg(json_build_object(
                                        'id', payment_proof.id,
                                        'file', payment_proof.file,
                                        'extension', payment_proof.extension,
                                        'time', payment_proof.time
                                    ))
                                    FROM payment_proof
                                    WHERE payment_proof.order_id = "order".id
                                )
                            ) as item
                    FROM "order"
                    WHERE "order".id in (
                        SELECT order_status.order_id
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT DISTINCT ON (order_status.order_id) order_status.id
                            FROM order_status
                            ORDER BY order_status.order_id, order_status.time DESC
                        )
                        AND order_status.status = 'WAITING_FOR_PAYMENT_CONFIRMATION'
                    )
                    AND "order".id in (
                        SELECT DISTINCT order_item.order_id
                        FROM order_item
                        JOIN warehouse_product ON order_item.product_id = warehouse_product.product_id
                        JOIN warehouse_admin ON warehouse_product.warehouse_id = warehouse_admin.warehouse_id
                        WHERE order_item.order_id = "order".id
                        AND warehouse_admin.account_id = ?
                    )
                    LIMIT ?
                    OFFSET ?
                ) as sq1
                ORDER BY SIMILARITY(sq1.item::text, ?) DESC
                """;

        return oneTemplate
                .query(sql,
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
                        page * size,
                        search
                );
    }


    public List<OrderResponse> getPaymentConfirmationOrders(Integer page, Integer size, String search) {
        String sql = """
                SELECT *
                FROM (
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
                                ),
                                'paymentProofs', (
                                    SELECT json_agg(json_build_object(
                                        'id', payment_proof.id,
                                        'file', payment_proof.file,
                                        'extension', payment_proof.extension,
                                        'time', payment_proof.time
                                    ))
                                    FROM payment_proof
                                    WHERE payment_proof.order_id = "order".id
                                )
                            ) as item
                    FROM "order"
                    WHERE "order".id in (
                        SELECT order_status.order_id
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT DISTINCT ON (order_status.order_id) order_status.id
                            FROM order_status
                            ORDER BY order_status.order_id, order_status.time DESC
                        )
                        AND order_status.status = 'WAITING_FOR_PAYMENT_CONFIRMATION'
                    )
                    LIMIT ?
                    OFFSET ?
                ) as sq1
                ORDER BY SIMILARITY(sq1.item::text, ?) DESC
                """;

        return oneTemplate
                .query(sql,
                        (rs, rowNum) -> {
                            try {
                                return objectMapper.readValue(rs.getString("item"), new TypeReference<>() {
                                });
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        size,
                        page * size,
                        search
                );
    }
}