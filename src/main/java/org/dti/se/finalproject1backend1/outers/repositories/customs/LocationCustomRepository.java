package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseProduct;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class LocationCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public Warehouse getNearestWarehouse(Point location) {
        String sql = """
                SELECT json_build_object(
                    'id', id,
                    'name', name,
                    'description', description,
                    'location', location
                ) AS item
                FROM warehouse
                ORDER BY location <-> ST_SetSRID(ST_MakePoint(?, ?), 4326) ASC
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
                            location.getX(),
                            location.getY()
                    );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public WarehouseProduct getNearestExistingWarehouseProduct(Point location, UUID productId) {
        String sql = """
                SELECT json_build_object(
                    'id', warehouse_product.id,
                    'quantity', warehouse_product.quantity,
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
                    ),
                    'warehouse', json_build_object(
                        'id', warehouse.id,
                        'name', warehouse.name,
                        'description', warehouse.description,
                        'location', warehouse.location
                    )
                ) AS item
                FROM warehouse_product
                JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                JOIN product ON warehouse_product.product_id = product.id
                JOIN category ON product.category_id = category.id
                WHERE product.id = ?
                ORDER BY warehouse.location <-> ST_SetSRID(ST_MakePoint(?, ?), 4326) ASC
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
                            productId,
                            location.getX(),
                            location.getY()
                    );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}