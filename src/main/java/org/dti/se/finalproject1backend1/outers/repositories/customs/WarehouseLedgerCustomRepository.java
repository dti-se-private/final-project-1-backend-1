package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.WarehouseLedgerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public class WarehouseLedgerCustomRepository {
    @Autowired
    @Qualifier("oneTemplate")
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public List<WarehouseLedgerResponse> getWarehouseLedgers(
            Account account,
            Integer page,
            Integer size,
            String search
    ) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                        'id', wl.id,
                        'origin_warehouse_product', json_build_object(
                            'id', wp1.id,
                            'quantity', wp1.quantity,
                            'warehouse', json_build_object(
                                'id', w1.id,
                                'name', w1.name,
                                'description', w1.description,
                                'location', w1.location
                            ),
                            'product', json_build_object(
                                'id', p1.id,
                                'name', p1.name,
                                'description', p1.description,
                                'price', p1.price,
                                'image', p1.image,
                                'category', json_build_object(
                                    'id', c1.id,
                                    'name', c1.name,
                                    'description', c1.description
                                    )
                                ),
                         ),
                         'destination_warehouse_product', json_build_object(
                            'id', wp2.id,
                            'quantity', wp2.quantity,
                            'warehouse', json_build_object(
                                'id', w2.id,
                                'name', w2.name,
                                'description', w2.description,
                                'location', w2.location
                            ),
                            'product', json_build_object(
                                'id', p2.id,
                                'name', p2.name,
                                'description', p2.description,
                                'price', p2.price,
                                'image', p2.image,
                                'category', json_build_object(
                                    'id', c2.id,
                                    'name', c2.name,
                                    'description', c2.description
                                    )
                                ),
                        ),
                        'origin_pre_quantity', wl.origin_pre_quantity,
                        'origin_post_quantity', wl.origin_post_quantity,
                        'destination_pre_quantity', wl.destination_pre_quantity,
                        'destination_post_quantity', wl.destination_post_quantity,
                        'time', wl.time,
                        'status', wl.status
                    ) as item
                    FROM warehouse_ledger wl
                    INNER JOIN warehouse_product wp1 ON wl.origin_warehouse_product_id = wp1.id
                    INNER JOIN warehouse_product wp2 ON wl.destination_warehouse_product_id = wp2.id
                    INNER JOIN product p1 ON p1.id = wp1.product_id
                    INNER JOIN product p2 ON p2.id = wp2.product_id
                    INNER JOIN category c1 ON c1.id = p1.category_id
                    INNER JOIN category c2 ON c2.id = p2.category_id
                    INNER JOIN warehouse w1 ON w1.id = wp1.warehouse_id
                    INNER JOIN warehouse w2 ON w2.id = wp2.warehouse_id
                    INNER JOIN warehouse_admin wa1 ON wa1.warehouse_id = w1.id
                    INNER JOIN warehouse_admin wa2 ON wa2.warehouse_id = w2.id
                    WHERE wa1.account_id = ? OR wa2.account_id = ?
                ) as sq1
                ORDER BY SIMILARITY(sq1.item::text, ?) DESC
                LIMIT ?
                OFFSET ?
                """;

        return oneTemplate.query(
                sql,
                (rs, rowNum) -> {
                    try {
                        return objectMapper.readValue(
                                rs.getString("item"),
                                new TypeReference<>() {}
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                account.getId(),
                search,
                size,
                page * size
        );
    }

    public void approveMutation(UUID id) {
        oneTemplate.update("""
            UPDATE warehouse_ledger 
            SET status = 'APPROVED'
            WHERE id = ?
            """, id);
    }

    public void rejectMutation(UUID id) {
        oneTemplate.update("""
            UPDATE warehouse_ledger 
            SET status = 'REJECTED'
            WHERE id = ?
            """, id);
    }

    public WarehouseLedgerResponse addMutation(
            UUID productId,
            UUID originWarehouseId,
            UUID destinationWarehouseId,
            Double quantity
    ) {
        String sql = """
                WITH warehouse_ledger AS (
                       INSERT INTO warehouse_ledger (
                            id,
                            origin_warehouse_product_id, destination_warehouse_product_id,
                            origin_pre_quantity, origin_post_quantity,
                            destination_pre_quantity, destination_post_quantity,
                            time, status
                       )
                       VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), 'PENDING')
                       RETURNING *
                   )
                    SELECT *
                    FROM (
                       SELECT json_build_object(
                            'id', wl.id,
                            'origin_warehouse_product', json_build_object(
                                'id', wp1.id,
                                'quantity', wp1.quantity,
                                'warehouse', json_build_object(
                                    'id', w1.id,
                                    'name', w1.name,
                                    'description', w1.description,
                                    'location', w1.location
                                ),
                                'product', json_build_object(
                                    'id', p1.id,
                                    'name', p1.name,
                                    'description', p1.description,
                                    'price', p1.price,
                                    'image', p1.image,
                                    'category', json_build_object(
                                        'id', c1.id,
                                        'name', c1.name,
                                        'description', c1.description
                                        )
                                    ),
                             ),
                             'destination_warehouse_product', json_build_object(
                                'id', wp2.id,
                                'quantity', wp2.quantity,
                                'warehouse', json_build_object(
                                    'id', w2.id,
                                    'name', w2.name,
                                    'description', w2.description,
                                    'location', w2.location
                                ),
                                'product', json_build_object(
                                    'id', p2.id,
                                    'name', p2.name,
                                    'description', p2.description,
                                    'price', p2.price,
                                    'image', p2.image,
                                    'category', json_build_object(
                                        'id', c2.id,
                                        'name', c2.name,
                                        'description', c2.description
                                        )
                                    ),
                            ),
                            'origin_pre_quantity', wl.origin_pre_quantity,
                            'origin_post_quantity', wl.origin_post_quantity,
                            'destination_pre_quantity', wl.destination_pre_quantity,
                            'destination_post_quantity', wl.destination_post_quantity,
                            'time', wl.time,
                            'status', wl.status
                       ) as item
                       FROM warehouse_ledger wl
                       INNER JOIN warehouse_product wp1 ON wl.origin_warehouse_product_id = wp1.id
                       INNER JOIN warehouse_product wp2 ON wl.destination_warehouse_product_id = wp2.id
                       INNER JOIN product p1 ON p1.id = wp1.product_id
                       INNER JOIN product p2 ON p2.id = wp2.product_id
                       INNER JOIN category c1 ON c1.id = p1.category_id
                       INNER JOIN category c2 ON c2.id = p2.category_id
                       INNER JOIN warehouse w1 ON w1.id = wp1.warehouse_id
                       INNER JOIN warehouse w2 ON w2.id = wp2.warehouse_id
                    ) as sq1
                    ORDER BY SIMILARITY(sq1.item::text, ?) DESC
                    LIMIT ?
                    OFFSET ?
                """;

        return oneTemplate.queryForObject(
                sql,
                (rs, rowNum) -> {
                    try {
                        return objectMapper.readValue(
                                rs.getString("ledger"),
                                new TypeReference<WarehouseLedgerResponse>() {}
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                UUID.randomUUID(),
                productId,
                originWarehouseId,
                destinationWarehouseId,
                quantity,
                quantity
        );
    }
}
