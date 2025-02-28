package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseadmins.WarehouseAdminResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class WarehouseAdminCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public List<WarehouseAdminResponse> getWarehouseAdmins(
            Integer page,
            Integer size,
            String search
    ) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                        'id', warehouse_admin.id,
                        'warehouse', json_build_object(
                            'id', warehouse.id,
                            'name', warehouse.name,
                            'description', warehouse.description,
                            'location', warehouse.location
                        ),
                        'account', json_build_object(
                            'id', account.id,
                            'name', account.name,
                            'email', account.email,
                            'password', account.password,
                            'phone', account.phone,
                            'image', account.image,
                            'is_verified', account.is_verified
                        )
                    ) as item
                    FROM warehouse_admin
                    INNER JOIN warehouse ON warehouse_admin.warehouse_id = warehouse.id
                    INNER JOIN account ON warehouse_admin.account_id = account.id
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

    public WarehouseAdminResponse getWarehouseAdmin(UUID warehouseAdminId) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                        'id', warehouse_admin.id,
                        'warehouse', json_build_object(
                            'id', warehouse.id,
                            'name', warehouse.name,
                            'description', warehouse.description,
                            'location', warehouse.location
                        ),
                        'account', json_build_object(
                            'id', account.id,
                            'name', account.name,
                            'email', account.email,
                            'password', account.password,
                            'phone', account.phone,
                            'image', account.image,
                            'is_verified', account.is_verified
                        )
                    ) as item
                    FROM warehouse_admin
                    INNER JOIN warehouse ON warehouse_admin.warehouse_id = warehouse.id
                    INNER JOIN account ON warehouse_admin.account_id = account.id
                    WHERE warehouse_admin.id = ?
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
                            warehouseAdminId
                    );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}