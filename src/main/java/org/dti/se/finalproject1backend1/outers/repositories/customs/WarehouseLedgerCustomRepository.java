package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.WarehouseLedgerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
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

    public Boolean isAccountRelatedToOriginWarehouseLedger(Account account, UUID warehouseLedgerId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM warehouse_ledger
                    INNER JOIN warehouse_product AS warehouse_product_origin ON warehouse_ledger.origin_warehouse_product_id = warehouse_product_origin.id
                    INNER JOIN warehouse AS warehouse_origin ON warehouse_origin.id = warehouse_product_origin.warehouse_id
                    WHERE warehouse_origin.id in (
                        SELECT DISTINCT warehouse_admin.warehouse_id
                        FROM warehouse_admin
                        WHERE warehouse_admin.warehouse_id = warehouse_origin.id
                        AND warehouse_admin.account_id = ?
                    )
                    AND warehouse_ledger.id = ?
                )
                """;

        return oneTemplate.queryForObject(sql, Boolean.class, account.getId(), warehouseLedgerId);
    }

    public List<WarehouseLedgerResponse> getOriginWarehouseLedgers(
            Account account,
            Integer page,
            Integer size,
            String search
    ) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                        'id', warehouse_ledger.id,
                        'origin_warehouse_product', origin_warehouse_product.details,
                        'destination_warehouse_product', destination_warehouse_product.details,
                        'origin_pre_quantity', warehouse_ledger.origin_pre_quantity,
                        'origin_post_quantity', warehouse_ledger.origin_post_quantity,
                        'destination_pre_quantity', warehouse_ledger.destination_pre_quantity,
                        'destination_post_quantity', warehouse_ledger.destination_post_quantity,
                        'time', warehouse_ledger.time,
                        'status', warehouse_ledger.status
                    ) AS item
                    FROM warehouse_ledger,
                    LATERAL (
                        SELECT json_build_object(
                            'id', warehouse_product.id,
                            'quantity', warehouse_product.quantity,
                            'warehouse', json_build_object(
                                'id', warehouse.id,
                                'name', warehouse.name,
                                'description', warehouse.description,
                                'location', warehouse.location
                            ),
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
                        ) AS details
                        FROM warehouse_product
                        INNER JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                        INNER JOIN product ON warehouse_product.product_id = product.id
                        INNER JOIN category ON product.category_id = category.id
                        WHERE warehouse_product.id = warehouse_ledger.origin_warehouse_product_id
                        AND warehouse.id in (
                            SELECT DISTINCT warehouse_admin.warehouse_id
                            FROM warehouse_admin
                            WHERE warehouse_admin.warehouse_id = warehouse.id
                            AND warehouse_admin.account_id = ?
                        )
                    ) AS origin_warehouse_product,
                    LATERAL (
                        SELECT json_build_object(
                            'id', warehouse_product.id,
                            'quantity', warehouse_product.quantity,
                            'warehouse', json_build_object(
                                'id', warehouse.id,
                                'name', warehouse.name,
                                'description', warehouse.description,
                                'location', warehouse.location
                            ),
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
                        ) AS details
                        FROM warehouse_product
                        INNER JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                        INNER JOIN product ON warehouse_product.product_id = product.id
                        INNER JOIN category ON product.category_id = category.id
                        WHERE warehouse_product.id = warehouse_ledger.destination_warehouse_product_id
                    ) AS destination_warehouse_product
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
                                new TypeReference<>() {
                                }
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

    public List<WarehouseLedgerResponse> getOriginWarehouseLedgers(
            Integer page,
            Integer size,
            String search
    ) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                        'id', warehouse_ledger.id,
                        'origin_warehouse_product', origin_warehouse_product.details,
                        'destination_warehouse_product', destination_warehouse_product.details,
                        'origin_pre_quantity', warehouse_ledger.origin_pre_quantity,
                        'origin_post_quantity', warehouse_ledger.origin_post_quantity,
                        'destination_pre_quantity', warehouse_ledger.destination_pre_quantity,
                        'destination_post_quantity', warehouse_ledger.destination_post_quantity,
                        'time', warehouse_ledger.time,
                        'status', warehouse_ledger.status
                    ) AS item
                    FROM warehouse_ledger,
                    LATERAL (
                        SELECT json_build_object(
                            'id', warehouse_product.id,
                            'quantity', warehouse_product.quantity,
                            'warehouse', json_build_object(
                                'id', warehouse.id,
                                'name', warehouse.name,
                                'description', warehouse.description,
                                'location', warehouse.location
                            ),
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
                        ) AS details
                        FROM warehouse_product
                        INNER JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                        INNER JOIN product ON warehouse_product.product_id = product.id
                        INNER JOIN category ON product.category_id = category.id
                        WHERE warehouse_product.id = warehouse_ledger.origin_warehouse_product_id
                    ) AS origin_warehouse_product,
                    LATERAL (
                        SELECT json_build_object(
                            'id', warehouse_product.id,
                            'quantity', warehouse_product.quantity,
                            'warehouse', json_build_object(
                                'id', warehouse.id,
                                'name', warehouse.name,
                                'description', warehouse.description,
                                'location', warehouse.location
                            ),
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
                        ) AS details
                        FROM warehouse_product
                        INNER JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                        INNER JOIN product ON warehouse_product.product_id = product.id
                        INNER JOIN category ON product.category_id = category.id
                        WHERE warehouse_product.id = warehouse_ledger.destination_warehouse_product_id
                    ) AS destination_warehouse_product
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
                                new TypeReference<>() {
                                }
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                search,
                size,
                page * size
        );
    }

    public WarehouseLedgerResponse getOriginWarehouseLedger(
            Account account,
            UUID warehouseLedgerId
    ) {
        String sql = """
                SELECT json_build_object(
                    'id', warehouse_ledger.id,
                    'origin_warehouse_product', origin_warehouse_product.details,
                    'destination_warehouse_product', destination_warehouse_product.details,
                    'origin_pre_quantity', warehouse_ledger.origin_pre_quantity,
                    'origin_post_quantity', warehouse_ledger.origin_post_quantity,
                    'destination_pre_quantity', warehouse_ledger.destination_pre_quantity,
                    'destination_post_quantity', warehouse_ledger.destination_post_quantity,
                    'time', warehouse_ledger.time,
                    'status', warehouse_ledger.status
                ) AS item
                FROM warehouse_ledger,
                LATERAL (
                    SELECT json_build_object(
                        'id', warehouse_product.id,
                        'quantity', warehouse_product.quantity,
                        'warehouse', json_build_object(
                            'id', warehouse.id,
                            'name', warehouse.name,
                            'description', warehouse.description,
                            'location', warehouse.location
                        ),
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
                    ) AS details
                    FROM warehouse_product
                    INNER JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                    INNER JOIN product ON warehouse_product.product_id = product.id
                    INNER JOIN category ON product.category_id = category.id
                    WHERE warehouse_product.id = warehouse_ledger.origin_warehouse_product_id
                    AND warehouse.id in (
                        SELECT DISTINCT warehouse_admin.warehouse_id
                        FROM warehouse_admin
                        WHERE warehouse_admin.warehouse_id = warehouse.id
                        AND warehouse_admin.account_id = ?
                    )
                ) AS origin_warehouse_product,
                LATERAL (
                    SELECT json_build_object(
                        'id', warehouse_product.id,
                        'quantity', warehouse_product.quantity,
                        'warehouse', json_build_object(
                            'id', warehouse.id,
                            'name', warehouse.name,
                            'description', warehouse.description,
                            'location', warehouse.location
                        ),
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
                    ) AS details
                    FROM warehouse_product
                    INNER JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                    INNER JOIN product ON warehouse_product.product_id = product.id
                    INNER JOIN category ON product.category_id = category.id
                    WHERE warehouse_product.id = warehouse_ledger.destination_warehouse_product_id
                ) AS destination_warehouse_product
                WHERE warehouse_ledger.id = ?
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
                            account.getId(),
                            warehouseLedgerId
                    );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public WarehouseLedgerResponse getOriginWarehouseLedger(
            UUID warehouseLedgerId
    ) {
        String sql = """
                SELECT json_build_object(
                    'id', warehouse_ledger.id,
                    'origin_warehouse_product', origin_warehouse_product.details,
                    'destination_warehouse_product', destination_warehouse_product.details,
                    'origin_pre_quantity', warehouse_ledger.origin_pre_quantity,
                    'origin_post_quantity', warehouse_ledger.origin_post_quantity,
                    'destination_pre_quantity', warehouse_ledger.destination_pre_quantity,
                    'destination_post_quantity', warehouse_ledger.destination_post_quantity,
                    'time', warehouse_ledger.time,
                    'status', warehouse_ledger.status
                ) AS item
                FROM warehouse_ledger,
                LATERAL (
                    SELECT json_build_object(
                        'id', warehouse_product.id,
                        'quantity', warehouse_product.quantity,
                        'warehouse', json_build_object(
                            'id', warehouse.id,
                            'name', warehouse.name,
                            'description', warehouse.description,
                            'location', warehouse.location
                        ),
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
                    ) AS details
                    FROM warehouse_product
                    INNER JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                    INNER JOIN product ON warehouse_product.product_id = product.id
                    INNER JOIN category ON product.category_id = category.id
                    WHERE warehouse_product.id = warehouse_ledger.origin_warehouse_product_id
                ) AS origin_warehouse_product,
                LATERAL (
                    SELECT json_build_object(
                        'id', warehouse_product.id,
                        'quantity', warehouse_product.quantity,
                        'warehouse', json_build_object(
                            'id', warehouse.id,
                            'name', warehouse.name,
                            'description', warehouse.description,
                            'location', warehouse.location
                        ),
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
                    ) AS details
                    FROM warehouse_product
                    INNER JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                    INNER JOIN product ON warehouse_product.product_id = product.id
                    INNER JOIN category ON product.category_id = category.id
                    WHERE warehouse_product.id = warehouse_ledger.destination_warehouse_product_id
                ) AS destination_warehouse_product
                WHERE warehouse_ledger.id = ?
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
                            warehouseLedgerId
                    );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
