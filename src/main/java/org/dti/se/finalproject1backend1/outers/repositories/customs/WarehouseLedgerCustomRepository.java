package org.dti.se.finalproject1backend1.outers.repositories.customs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            List<UUID> warehouseIds,
            Integer page,
            Integer size,
            String search
    ) {
        String sql = """
            SELECT json_build_object(
                'id', wl.id,
                'product', json_build_object(
                        'id', p.id,
                        'name', p.name,
                        'description', p.description,
                        'price', p.price,
                        'image', p.image,
                        'category', json_build_object(
                            'id', c.id,
                            'name', c.name,
                            'description', c.description
                        )
                    ),
                'origin_warehouse', json_build_object(
                     'id', w1.id,
                     'name', w1.name,
                     'description', w1.description,
                     'location', w1.location
                 ),
                 'destination_warehouse', json_build_object(
                     'id', w2.id,
                     'name', w2.name,
                     'description', w2.description,
                     'location', w2.location
                 ),
                 'origin_pre_quantity', wl.origin_pre_quantity,
                 'origin_post_quantity', wl.origin_post_quantity,
                 'destination_pre_quantity', wl.destination_pre_quantity,
                 'destination_post_quantity', wl.destination_post_quantity,
                 'time', wl.time,
                 'status', wl.status
            ) as ledger
            FROM warehouse_ledger wl
            JOIN product p ON wl.product_id = p.id
            JOIN category c ON p.category_id = c.id
            JOIN warehouse w1 ON wl.origin_warehouse_id = w1.id
            JOIN warehouse w2 ON wl.destination_warehouse_id = w2.id
            WHERE (w1.id IN (:warehouseIds) OR w2.id IN (:warehouseIds))
              AND (wl.id::text ILIKE ? OR
                   w1.name ILIKE ? OR
                   w2.name ILIKE ? OR
                  p.name ILIKE ? OR 
                  c.name ILIKE ?
            ORDER BY wl.time DESC
            LIMIT ?
            OFFSET ?
            """;

        return oneTemplate.query(
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
                warehouseIds,
                "%" + search + "%",
                "%" + search + "%",
                "%" + search + "%",
                "%" + search + "%",
                "%" + search + "%",
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
            WITH inserted AS (
                   INSERT INTO warehouse_ledger (
                       id, product_id, origin_warehouse_id,\s
                       destination_warehouse_id, origin_pre_quantity,\s
                       origin_post_quantity, time, status
                   )
                   VALUES (?, ?, ?, ?, ?, ?, NOW(), 'PENDING')
                   RETURNING *
               )
               SELECT json_build_object(
                   'id', i.id,
                   'product', json_build_object(
                       'id', p.id,
                       'name', p.name,
                       'description', p.description,
                       'price', p.price,
                       'image', p.image,
                       'category', json_build_object(
                           'id', c.id,
                           'name', c.name,
                           'description', c.description
                       )
                   ),
                   'origin_warehouse', json_build_object(
                       'id', w1.id,
                       'name', w1.name,
                       'description', w1.description,
                       'location', w1.location
                   ),
                   'destination_warehouse', json_build_object(
                       'id', w2.id,
                       'name', w2.name,
                       'description', w2.description,
                       'location', w2.location
                   ),
                   'origin_pre_quantity', i.origin_pre_quantity,
                   'origin_post_quantity', i.origin_post_quantity,
                   'destination_pre_quantity', i.destination_pre_quantity,
                   'destination_post_quantity', i.destination_post_quantity,
                   'time', i.time,
                   'status', i.status
               ) as ledger
               FROM inserted i
               JOIN product p ON i.product_id = p.id
               JOIN category c ON p.category_id = c.id
               JOIN warehouse w1 ON i.origin_warehouse_id = w1.id
               JOIN warehouse w2 ON i.destination_warehouse_id = w2.id
               ORDER BY %s
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
