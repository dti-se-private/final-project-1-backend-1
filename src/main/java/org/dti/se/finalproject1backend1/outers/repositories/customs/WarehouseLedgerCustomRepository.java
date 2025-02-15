package org.dti.se.finalproject1backend1.outers.repositories.customs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.WarehouseLedgerResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.WarehouseResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;
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
            Integer page,
            Integer size,
            String search
    ) {
        String sql = """
            SELECT json_build_object(
                'id', wl.id,
                'warehouse_product', json_build_object(
                    'id', wp.id,
                    'warehouse', json_build_object(
                        'id', w.id,
                        'name', w.name,
                        'description', w.description,
                        'location', w.location
                    ),
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
                    'quantity', wp.quantity
                ),
                'pre_quantity', wl.origin_pre_quantity,
                'post_quantity', wl.origin_post_quantity,
                'time', wl.time,
                'is_approved', wl.status = 'APPROVED'
            ) as ledger
            FROM warehouse_ledger wl
            JOIN warehouse_product wp ON wl.product_id = wp.product_id 
                AND wl.origin_warehouse_id = wp.warehouse_id
            JOIN warehouse w ON wp.warehouse_id = w.id
            JOIN product p ON wp.product_id = p.id
            JOIN category c ON p.category_id = c.id
            WHERE wl.id::text ILIKE ? OR 
                  w.name ILIKE ? OR 
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
                    id, product_id, origin_warehouse_id, 
                    destination_warehouse_id, origin_pre_quantity, 
                    origin_post_quantity, time, status
                )
                VALUES (?, ?, ?, ?, ?, ?, NOW(), 'PENDING')
                RETURNING *
            )
            SELECT json_build_object(
                'id', i.id,
                'warehouse_product', json_build_object(
                    'id', wp.id,
                    'warehouse', json_build_object(
                        'id', w.id,
                        'name', w.name,
                        'description', w.description,
                        'location', w.location
                    ),
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
                    'quantity', wp.quantity
                ),
                'pre_quantity', i.origin_pre_quantity,
                'post_quantity', i.origin_post_quantity,
                'time', i.time,
                'is_approved', i.status = 'APPROVED'
            ) as ledger
            FROM inserted i
            JOIN warehouse_product wp ON i.product_id = wp.product_id 
                AND i.origin_warehouse_id = wp.warehouse_id
            JOIN warehouse w ON wp.warehouse_id = w.id
            JOIN product p ON wp.product_id = p.id
            JOIN category c ON p.category_id = c.id
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
