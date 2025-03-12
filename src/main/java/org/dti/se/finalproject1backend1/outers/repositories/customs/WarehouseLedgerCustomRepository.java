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
                        'origin_warehouse_product', json_build_object(
                            'id', warehouse_product_origin.id,
                            'quantity', warehouse_product_origin.quantity,
                            'warehouse', json_build_object(
                                'id', warehouse_origin.id,
                                'name', warehouse_origin.name,
                                'description', warehouse_origin.description,
                                'location', warehouse_origin.location
                            ),
                            'product', json_build_object(
                                'id', product_origin.id,
                                'name', product_origin.name,
                                'description', product_origin.description,
                                'price', product_origin.price,
                                'weight', product_origin.weight,
                                'image', product_origin.image,
                                'category', json_build_object(
                                    'id', category_origin.id,
                                    'name', category_origin.name,
                                    'description', category_origin.description
                                )
                            )
                        ),
                        'destination_warehouse_product', json_build_object(
                            'id', warehouse_product_destination.id,
                            'quantity', warehouse_product_destination.quantity,
                            'warehouse', json_build_object(
                                'id', warehouse_destination.id,
                                'name', warehouse_destination.name,
                                'description', warehouse_destination.description,
                                'location', warehouse_destination.location
                            ),
                            'product', json_build_object(
                                'id', product_destination.id,
                                'name', product_destination.name,
                                'description', product_destination.description,
                                'price', product_destination.price,
                                'weight', product_destination.weight,
                                'image', product_destination.image,
                                'category', json_build_object(
                                    'id', category_destination.id,
                                    'name', category_destination.name,
                                    'description', category_destination.description
                                )
                            )
                        ),
                        'origin_pre_quantity', warehouse_ledger.origin_pre_quantity,
                        'origin_post_quantity', warehouse_ledger.origin_post_quantity,
                        'destination_pre_quantity', warehouse_ledger.destination_pre_quantity,
                        'destination_post_quantity', warehouse_ledger.destination_post_quantity,
                        'time', warehouse_ledger.time,
                        'status', warehouse_ledger.status
                    ) as item
                    FROM warehouse_ledger
                    INNER JOIN warehouse_product AS warehouse_product_origin ON warehouse_ledger.origin_warehouse_product_id = warehouse_product_origin.id
                    INNER JOIN warehouse_product AS warehouse_product_destination ON warehouse_ledger.destination_warehouse_product_id = warehouse_product_destination.id
                    INNER JOIN product AS product_origin ON product_origin.id = warehouse_product_origin.product_id
                    INNER JOIN product AS product_destination ON product_destination.id = warehouse_product_destination.product_id
                    INNER JOIN category AS category_origin ON category_origin.id = product_origin.category_id
                    INNER JOIN category AS category_destination ON category_destination.id = product_destination.category_id
                    INNER JOIN warehouse AS warehouse_origin ON warehouse_origin.id = warehouse_product_origin.warehouse_id
                    INNER JOIN warehouse AS warehouse_destination ON warehouse_destination.id = warehouse_product_destination.warehouse_id
                    WHERE warehouse_origin.id in (
                        SELECT DISTINCT warehouse_admin.warehouse_id
                        FROM warehouse_admin
                        WHERE warehouse_admin.warehouse_id = warehouse_origin.id
                        AND warehouse_admin.account_id = ?
                    )
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
                        'origin_warehouse_product', json_build_object(
                            'id', warehouse_product_origin.id,
                            'quantity', warehouse_product_origin.quantity,
                            'warehouse', json_build_object(
                                'id', warehouse_origin.id,
                                'name', warehouse_origin.name,
                                'description', warehouse_origin.description,
                                'location', warehouse_origin.location
                            ),
                            'product', json_build_object(
                                'id', product_origin.id,
                                'name', product_origin.name,
                                'description', product_origin.description,
                                'price', product_origin.price,
                                'weight', product_origin.weight,
                                'image', product_origin.image,
                                'category', json_build_object(
                                    'id', category_origin.id,
                                    'name', category_origin.name,
                                    'description', category_origin.description
                                )
                            )
                        ),
                        'destination_warehouse_product', json_build_object(
                            'id', warehouse_product_destination.id,
                            'quantity', warehouse_product_destination.quantity,
                            'warehouse', json_build_object(
                                'id', warehouse_destination.id,
                                'name', warehouse_destination.name,
                                'description', warehouse_destination.description,
                                'location', warehouse_destination.location
                            ),
                            'product', json_build_object(
                                'id', product_destination.id,
                                'name', product_destination.name,
                                'description', product_destination.description,
                                'price', product_destination.price,
                                'weight', product_destination.weight,
                                'image', product_destination.image,
                                'category', json_build_object(
                                    'id', category_destination.id,
                                    'name', category_destination.name,
                                    'description', category_destination.description
                                )
                            )
                        ),
                        'origin_pre_quantity', warehouse_ledger.origin_pre_quantity,
                        'origin_post_quantity', warehouse_ledger.origin_post_quantity,
                        'destination_pre_quantity', warehouse_ledger.destination_pre_quantity,
                        'destination_post_quantity', warehouse_ledger.destination_post_quantity,
                        'time', warehouse_ledger.time,
                        'status', warehouse_ledger.status
                    ) as item
                    FROM warehouse_ledger
                    INNER JOIN warehouse_product AS warehouse_product_origin ON warehouse_ledger.origin_warehouse_product_id = warehouse_product_origin.id
                    INNER JOIN warehouse_product AS warehouse_product_destination ON warehouse_ledger.destination_warehouse_product_id = warehouse_product_destination.id
                    INNER JOIN product AS product_origin ON product_origin.id = warehouse_product_origin.product_id
                    INNER JOIN product AS product_destination ON product_destination.id = warehouse_product_destination.product_id
                    INNER JOIN category AS category_origin ON category_origin.id = product_origin.category_id
                    INNER JOIN category AS category_destination ON category_destination.id = product_destination.category_id
                    INNER JOIN warehouse AS warehouse_origin ON warehouse_origin.id = warehouse_product_origin.warehouse_id
                    INNER JOIN warehouse AS warehouse_destination ON warehouse_destination.id = warehouse_product_destination.warehouse_id
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
                    'origin_warehouse_product', json_build_object(
                        'id', warehouse_product_origin.id,
                        'quantity', warehouse_product_origin.quantity,
                        'warehouse', json_build_object(
                            'id', warehouse_origin.id,
                            'name', warehouse_origin.name,
                            'description', warehouse_origin.description,
                            'location', warehouse_origin.location
                        ),
                        'product', json_build_object(
                            'id', product_origin.id,
                            'name', product_origin.name,
                            'description', product_origin.description,
                            'price', product_origin.price,
                            'weight', product_origin.weight,
                            'image', product_origin.image,
                            'category', json_build_object(
                                'id', category_origin.id,
                                'name', category_origin.name,
                                'description', category_origin.description
                            )
                        )
                    ),
                    'destination_warehouse_product', json_build_object(
                        'id', warehouse_product_destination.id,
                        'quantity', warehouse_product_destination.quantity,
                        'warehouse', json_build_object(
                            'id', warehouse_destination.id,
                            'name', warehouse_destination.name,
                            'description', warehouse_destination.description,
                            'location', warehouse_destination.location
                        ),
                        'product', json_build_object(
                            'id', product_destination.id,
                            'name', product_destination.name,
                            'description', product_destination.description,
                            'price', product_destination.price,
                            'weight', product_destination.weight,
                            'image', product_destination.image,
                            'category', json_build_object(
                                'id', category_destination.id,
                                'name', category_destination.name,
                                'description', category_destination.description
                            )
                        )
                    ),
                    'origin_pre_quantity', warehouse_ledger.origin_pre_quantity,
                    'origin_post_quantity', warehouse_ledger.origin_post_quantity,
                    'destination_pre_quantity', warehouse_ledger.destination_pre_quantity,
                    'destination_post_quantity', warehouse_ledger.destination_post_quantity,
                    'time', warehouse_ledger.time,
                    'status', warehouse_ledger.status
                ) as item
                FROM warehouse_ledger
                INNER JOIN warehouse_product AS warehouse_product_origin ON warehouse_ledger.origin_warehouse_product_id = warehouse_product_origin.id
                INNER JOIN warehouse_product AS warehouse_product_destination ON warehouse_ledger.destination_warehouse_product_id = warehouse_product_destination.id
                INNER JOIN product AS product_origin ON product_origin.id = warehouse_product_origin.product_id
                INNER JOIN product AS product_destination ON product_destination.id = warehouse_product_destination.product_id
                INNER JOIN category AS category_origin ON category_origin.id = product_origin.category_id
                INNER JOIN category AS category_destination ON category_destination.id = product_destination.category_id
                INNER JOIN warehouse AS warehouse_origin ON warehouse_origin.id = warehouse_product_origin.warehouse_id
                INNER JOIN warehouse AS warehouse_destination ON warehouse_destination.id = warehouse_product_destination.warehouse_id
                WHERE warehouse_origin.id in (
                    SELECT DISTINCT warehouse_admin.warehouse_id
                    FROM warehouse_admin
                    WHERE warehouse_admin.warehouse_id = warehouse_origin.id
                    AND warehouse_admin.account_id = ?
                )
                AND warehouse_ledger.id = ?
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
                    'origin_warehouse_product', json_build_object(
                        'id', warehouse_product_origin.id,
                        'quantity', warehouse_product_origin.quantity,
                        'warehouse', json_build_object(
                            'id', warehouse_origin.id,
                            'name', warehouse_origin.name,
                            'description', warehouse_origin.description,
                            'location', warehouse_origin.location
                        ),
                        'product', json_build_object(
                            'id', product_origin.id,
                            'name', product_origin.name,
                            'description', product_origin.description,
                            'price', product_origin.price,
                            'weight', product_origin.weight,
                            'image', product_origin.image,
                            'category', json_build_object(
                                'id', category_origin.id,
                                'name', category_origin.name,
                                'description', category_origin.description
                            )
                        )
                    ),
                    'destination_warehouse_product', json_build_object(
                        'id', warehouse_product_destination.id,
                        'quantity', warehouse_product_destination.quantity,
                        'warehouse', json_build_object(
                            'id', warehouse_destination.id,
                            'name', warehouse_destination.name,
                            'description', warehouse_destination.description,
                            'location', warehouse_destination.location
                        ),
                        'product', json_build_object(
                            'id', product_destination.id,
                            'name', product_destination.name,
                            'description', product_destination.description,
                            'price', product_destination.price,
                            'weight', product_destination.weight,
                            'image', product_destination.image,
                            'category', json_build_object(
                                'id', category_destination.id,
                                'name', category_destination.name,
                                'description', category_destination.description
                            )
                        )
                    ),
                    'origin_pre_quantity', warehouse_ledger.origin_pre_quantity,
                    'origin_post_quantity', warehouse_ledger.origin_post_quantity,
                    'destination_pre_quantity', warehouse_ledger.destination_pre_quantity,
                    'destination_post_quantity', warehouse_ledger.destination_post_quantity,
                    'time', warehouse_ledger.time,
                    'status', warehouse_ledger.status
                ) as item
                FROM warehouse_ledger
                INNER JOIN warehouse_product AS warehouse_product_origin ON warehouse_ledger.origin_warehouse_product_id = warehouse_product_origin.id
                INNER JOIN warehouse_product AS warehouse_product_destination ON warehouse_ledger.destination_warehouse_product_id = warehouse_product_destination.id
                INNER JOIN product AS product_origin ON product_origin.id = warehouse_product_origin.product_id
                INNER JOIN product AS product_destination ON product_destination.id = warehouse_product_destination.product_id
                INNER JOIN category AS category_origin ON category_origin.id = product_origin.category_id
                INNER JOIN category AS category_destination ON category_destination.id = product_destination.category_id
                INNER JOIN warehouse AS warehouse_origin ON warehouse_origin.id = warehouse_product_origin.warehouse_id
                INNER JOIN warehouse AS warehouse_destination ON warehouse_destination.id = warehouse_product_destination.warehouse_id
                AND warehouse_ledger.id = ?
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
