package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouses.WarehouseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class WarehouseCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public List<WarehouseResponse> getWarehouses(
            Integer page,
            Integer size,
            String search
    ) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                        'id', warehouse.id,
                        'name', warehouse.name,
                        'description', warehouse.description,
                        'location', warehouse.location
                    ) as item
                    FROM warehouse
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


    public WarehouseResponse getWarehouse(UUID warehouseId) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                        'id', warehouse.id,
                        'name', warehouse.name,
                        'description', warehouse.description,
                        'location', warehouse.location
                    ) as item
                    FROM warehouse
                    WHERE warehouse.id = ?
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
                            warehouseId
                    );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<WarehouseResponse> getAccountWarehouses(
            Account account,
            Integer page,
            Integer size,
            String search
    ) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                        'id', warehouse.id,
                        'name', warehouse.name,
                        'description', warehouse.description,
                        'location', warehouse.location
                    ) as item
                    FROM warehouse
                    WHERE warehouse.id IN (
                        SELECT DISTINCT warehouse_admin.warehouse_id
                        FROM warehouse_admin
                        WHERE warehouse_admin.account_id = ?
                    )
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


    public WarehouseResponse getAccountWarehouse(Account account, UUID warehouseId) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                        'id', warehouse.id,
                        'name', warehouse.name,
                        'description', warehouse.description,
                        'location', warehouse.location
                    ) as item
                    FROM warehouse
                    WHERE warehouse.id IN (
                        SELECT DISTINCT warehouse_admin.warehouse_id
                        FROM warehouse_admin
                        WHERE warehouse_admin.account_id = ?
                    )
                    AND warehouse.id = ?
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
                            warehouseId
                    );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}