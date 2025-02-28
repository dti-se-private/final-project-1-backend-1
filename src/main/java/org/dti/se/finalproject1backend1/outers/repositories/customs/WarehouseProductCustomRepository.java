package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class WarehouseProductCustomRepository {
    @Autowired
    @Qualifier("oneTemplate")
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public List<WarehouseProductResponse> getWarehouseProducts(
            Integer page,
            Integer size,
            String search
    ) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                             'id', warehouse_product.id,
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
                                 'quantity', COALESCE((
                                     SELECT sum(warehouse_product.quantity)
                                     FROM warehouse_product
                                     WHERE warehouse_product.product_id = product.id
                                 ), 0),
                                 'category', json_build_object(
                                     'id', category.id,
                                     'name', category.name,
                                     'description', category.description
                                 )
                             ),
                             'quantity', warehouse_product.quantity
                    ) as item
                    FROM warehouse_product
                    INNER JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                    INNER JOIN product ON warehouse_product.product_id = product.id
                    INNER JOIN category ON product.category_id = category.id
                ) as sq1
                ORDER BY SIMILARITY(sq1.item::text, ?) DESC
                LIMIT ?
                OFFSET ?
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
                        search,
                        size,
                        page * size
                );
    }


    public List<WarehouseProductResponse> getAccountWarehouseProducts(
            Account account,
            Integer page,
            Integer size,
            String search
    ) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                             'id', warehouse_product.id,
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
                                 'quantity', COALESCE((
                                     SELECT sum(warehouse_product.quantity)
                                     FROM warehouse_product
                                     WHERE warehouse_product.product_id = product.id
                                 ), 0),
                                 'category', json_build_object(
                                     'id', category.id,
                                     'name', category.name,
                                     'description', category.description
                                 )
                             ),
                             'quantity', warehouse_product.quantity
                    ) as item
                    FROM warehouse_product
                    INNER JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                    INNER JOIN product ON warehouse_product.product_id = product.id
                    INNER JOIN category ON product.category_id = category.id
                    INNER JOIN warehouse_admin ON warehouse.id = warehouse_admin.warehouse_id
                    WHERE warehouse_admin.account_id = ?
                ) as sq1
                ORDER BY SIMILARITY(sq1.item::text, ?) DESC
                LIMIT ?
                OFFSET ?
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
                        search,
                        size,
                        page * size
                );
    }

    public WarehouseProductResponse getWarehouseProduct(UUID warehouseProductId) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                             'id', warehouse_product.id,
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
                                 'quantity', COALESCE((
                                     SELECT sum(warehouse_product.quantity)
                                     FROM warehouse_product
                                     WHERE warehouse_product.product_id = product.id
                                 ), 0),
                                 'category', json_build_object(
                                     'id', category.id,
                                     'name', category.name,
                                     'description', category.description
                                 )
                             ),
                             'quantity', warehouse_product.quantity
                    ) as item
                    FROM warehouse_product
                    INNER JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                    INNER JOIN product ON warehouse_product.product_id = product.id
                    INNER JOIN category ON product.category_id = category.id
                    WHERE warehouse_product.id = ?
                ) as sq1
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
                            warehouseProductId
                    );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    public WarehouseProductResponse getAccountWarehouseProduct(Account account, UUID warehouseProductId) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                             'id', warehouse_product.id,
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
                                 'quantity', COALESCE((
                                     SELECT sum(warehouse_product.quantity)
                                     FROM warehouse_product
                                     WHERE warehouse_product.product_id = product.id
                                 ), 0),
                                 'category', json_build_object(
                                     'id', category.id,
                                     'name', category.name,
                                     'description', category.description
                                 )
                             ),
                             'quantity', warehouse_product.quantity
                    ) as item
                    FROM warehouse_product
                    INNER JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                    INNER JOIN product ON warehouse_product.product_id = product.id
                    INNER JOIN category ON product.category_id = category.id
                    INNER JOIN warehouse_admin ON warehouse.id = warehouse_admin.warehouse_id
                    WHERE warehouse_admin.account_id = ?
                    AND warehouse_product.id = ?
                ) as sq1
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
                            warehouseProductId
                    );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
