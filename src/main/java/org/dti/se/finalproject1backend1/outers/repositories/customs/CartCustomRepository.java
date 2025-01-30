package org.dti.se.finalproject1backend1.outers.repositories.customs;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.carts.CartItemResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CartCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    private JdbcTemplate oneTemplate;

    public List<CartItemResponse> getCartItems(
            Account account,
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
            order = "cart_item_id";
        }

        String query = String.format("""
                SELECT
                p.id as product_id,
                p.category_id as category_id,
                p.name as product_name,
                p.description as product_description,
                p.price as product_price,
                p.image as product_image,
                c.id as category_id,
                c.name as category_name,
                c.description as category_description,
                ci.id as cart_item_id,
                ci.quantity as cart_item_quantity
                FROM cart_item ci
                JOIN product p ON ci.product_id = p.id
                JOIN category c ON p.category_id = c.id
                WHERE ci.account_id = ?
                ORDER BY %s
                LIMIT ?
                OFFSET ?
                """, order);
        return oneTemplate
                .query(query,
                        (rs, rowNum) -> {
                            CategoryResponse category = CategoryResponse
                                    .builder()
                                    .id(rs.getObject("category_id", java.util.UUID.class))
                                    .name(rs.getString("category_name"))
                                    .description(rs.getString("category_description"))
                                    .build();
                            ProductResponse product = ProductResponse
                                    .builder()
                                    .id(rs.getObject("product_id", java.util.UUID.class))
                                    .category(category)
                                    .name(rs.getString("product_name"))
                                    .description(rs.getString("product_description"))
                                    .price(rs.getDouble("product_price"))
                                    .image(rs.getBytes("product_image"))
                                    .build();
                            return CartItemResponse.builder()
                                    .id(rs.getObject("cart_item_id", java.util.UUID.class))
                                    .quantity(rs.getDouble("cart_item_quantity"))
                                    .product(product)
                                    .build();
                        },
                        account.getId(),
                        size,
                        page * size
                );

    }

    public void addCartItem(UUID accountId, UUID productId, Double quantity) {
        oneTemplate.update("""
                        INSERT INTO cart_item (id, account_id, product_id, quantity)
                        VALUES (?, ?, ?, ?)
                        ON CONFLICT (account_id, product_id) DO UPDATE
                        SET quantity = cart_item.quantity + ?
                        """,
                UUID.randomUUID(),
                accountId,
                productId,
                quantity,
                quantity
        );
    }

    public void removeCartItem(UUID accountId, UUID productId, Double quantity) {
        oneTemplate.update("""
                        UPDATE cart_item
                        SET quantity = cart_item.quantity - ?
                        WHERE account_id = ? AND product_id = ?
                        """,
                quantity,
                accountId,
                productId
        );

        oneTemplate.update("""
                        DELETE FROM cart_item
                        WHERE account_id = ? AND product_id = ? AND quantity <= 0
                        """,
                accountId,
                productId
        );
    }
}