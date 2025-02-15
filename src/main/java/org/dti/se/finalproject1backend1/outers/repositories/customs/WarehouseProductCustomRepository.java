package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.WarehouseResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class WarehouseProductCustomRepository {
    @Autowired
    @Qualifier("oneTemplate")
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public List<WarehouseProductResponse> getAllWarehouseProducts(
            Integer page,
            Integer size,
            List<String> filters,
            String search
    ) {
        String order = filters
                .stream()
                .map(filter -> String.format("SIMILARITY(%s::text, '%s')", filter, search))
                .collect(Collectors.joining("+"));

        if (order.isEmpty()) {
            order = "warehouse_product.id";
        }

        String sql = String.format("""
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
                            'category', json_build_object(
                                'id', category.id,
                                'name', category.name,
                                'description', category.description
                            )
                        ),
                        'quantity', warehouse_product.quantity
                    ) as warehouse_product
                FROM warehouse_product
                JOIN warehouse ON warehouse_product.warehouse_id = warehouse.id
                JOIN product ON warehouse_product.product_id = product.id
                JOIN category ON product.category_id = category.id
                ORDER BY %s
                LIMIT ?
                OFFSET ?
               """, order);

        return oneTemplate
                .query(sql,
                        (rs, rowNum) -> {
                            try {
                                return objectMapper.readValue(rs.getString("warehouse_product"), new TypeReference<>() {});
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        size,
                        page * size
                );
    }
}
