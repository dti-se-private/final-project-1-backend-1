package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ProductCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public List<CategoryResponse> getCategories(
            Integer page,
            Integer size,
            String search
    ) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                        'id', category.id,
                        'name', category.name,
                        'description', category.description
                    ) as item
                    FROM category
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

    public CategoryResponse getCategory(UUID categoryId) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                        'id', category.id,
                        'name', category.name,
                        'description', category.description
                    ) as item
                    FROM category
                    WHERE category.id = ?
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
                            categoryId
                    );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<ProductResponse> getProducts(
            Integer page,
            Integer size,
            String search
    ) {
        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                        'id', product.id,
                        'name', product.name,
                        'description', product.description,
                        'price', product.price,
                        'weight', product.weight,
                        'image', product.image,
                           'category', json_build_object(
                               'id', category.id,
                               'name', category.name,
                               'description', category.description
                           ),
                            'quantity', COALESCE((
                                SELECT sum(warehouse_product.quantity)
                                FROM warehouse_product
                                WHERE warehouse_product.product_id = product.id
                            ), 0)
                    ) as item
                    FROM category
                    INNER JOIN product ON category.id = product.category_id
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

    public ProductResponse getProduct(UUID productId) {
        String sql = """
                SELECT json_build_object(
                    'id', product.id,
                    'name', product.name,
                    'description', product.description,
                    'price', product.price,
                    'weight', product.weight,
                    'image', product.image,
                    'category', json_build_object(
                        'id', category.id,
                        'name', category.name,
                        'description', category.description
                    ),
                    'quantity', COALESCE((
                        SELECT sum(warehouse_product.quantity)
                        FROM warehouse_product
                        WHERE warehouse_product.product_id = product.id
                    ), 0)
                ) as item
                FROM product
                INNER JOIN category ON product.category_id = category.id
                WHERE product.id = ?
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
                            productId
                    );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}