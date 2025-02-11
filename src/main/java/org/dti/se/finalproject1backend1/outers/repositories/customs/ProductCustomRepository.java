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
import java.util.stream.Collectors;

@Repository
public class ProductCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    private JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public ProductResponse getAllWarehouseProduct(UUID productId) {
        CategoryResponse category = oneTemplate
                .queryForObject("""
                                SELECT
                                c.id as category_id,
                                c.name as category_name,
                                c.description as category_description
                                FROM product p
                                JOIN category c ON p.category_id = c.id
                                WHERE p.id = ?
                                LIMIT 1
                                """,
                        (rs, rowNum) -> CategoryResponse
                                .builder()
                                .id(UUID.fromString(rs.getString("category_id")))
                                .name(rs.getString("category_name"))
                                .description(rs.getString("category_description"))
                                .build(),
                        productId
                );

        try {
            return oneTemplate
                    .queryForObject("""
                                    SELECT
                                    p.id as product_id,
                                    p.name as product_name,
                                    p.description as product_description,
                                    p.price as product_price,
                                    p.image as product_image,
                                    sum(wp.quantity) as product_total_quantity
                                    FROM warehouse_product wp
                                    JOIN product p ON wp.product_id = p.id
                                    WHERE wp.product_id = ?
                                    GROUP BY p.id
                                    """,
                            (rs, rowNum) -> ProductResponse
                                    .builder()
                                    .id(UUID.fromString(rs.getString("product_id")))
                                    .category(category)
                                    .name(rs.getString("product_name"))
                                    .description(rs.getString("product_description"))
                                    .price(rs.getDouble("product_price"))
                                    .image(rs.getBytes("product_image"))
                                    .totalQuantity(rs.getDouble("product_total_quantity"))
                                    .build(),
                            productId
                    );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<ProductResponse> getProducts(
            int page,
            int size,
            List<String> filters,
            String search
    ) {
        String order = filters.stream()
                .map(filter -> String.format("SIMILARITY(%s::text, '%s')", filter, search))
                .collect(Collectors.joining("+"));

        if (order.isEmpty()) {
            order = "product.id";
        }

        String sql = String.format("""
            SELECT json_build_object(
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
            ) as item
            FROM product
            JOIN category ON product.category_id = category.id
            ORDER BY %s
            LIMIT ?
            OFFSET ?
            """, order);

        return oneTemplate.query(
                sql,
                (rs, rowNum) -> {
                    try {
                        return objectMapper.readValue(rs.getString("item"), new TypeReference<ProductResponse>() {});
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                size,
                page * size
        );
    }

    public ProductResponse getById(UUID id) {
        String sql = """
            SELECT json_build_object(
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
            ) as item
            FROM product
            JOIN category ON product.category_id = category.id
            WHERE product.id = ?
            """;

        return oneTemplate.queryForObject(
                sql,
                (rs, rowNum) -> {
                    try {
                        return objectMapper.readValue(rs.getString("item"), new TypeReference<ProductResponse>() {});
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                id
        );
    }

    public void create(ProductResponse product) {
        oneTemplate.update("""
            INSERT INTO product (id, name, description, price, image, category_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
                UUID.randomUUID(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImage(),
                product.getCategory().getId()
        );
    }

    public void update(ProductResponse product) {
        oneTemplate.update("""
            UPDATE product SET
                name = ?,
                description = ?,
                price = ?,
                image = ?,
                category_id = ?
            WHERE id = ?
            """,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImage(),
                product.getCategory().getId(),
                product.getId()
        );
    }

    public void delete(UUID id) {
        oneTemplate.update("DELETE FROM product WHERE id = ?", id);
    }
}