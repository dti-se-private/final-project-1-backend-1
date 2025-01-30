package org.dti.se.finalproject1backend1.outers.repositories.customs;

import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ProductCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    private JdbcTemplate oneTemplate;

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
    }
}