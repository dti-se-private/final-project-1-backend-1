package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.carts.CartItemResponse;
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
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

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
            order = "cart_item.id";
        }

        String sql = String.format("""
                SELECT json_build_object(
                        'id', cart_item.id,
                        'quantity', cart_item.quantity,
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
                        )
                    ) as item
                FROM cart_item
                JOIN product ON cart_item.product_id =product.id
                JOIN category ON product.category_id = category.id
                WHERE account_id = ?
                ORDER BY %s
                LIMIT ?
                OFFSET ?
                """, order);

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
                        size,
                        page * size
                );
    }

    public Double getTotalPrice(UUID accountId) {
        return oneTemplate
                .queryForObject("""
                                SELECT SUM(product.price * cart_item.quantity)
                                FROM cart_item
                                JOIN product ON cart_item.product_id = product.id
                                WHERE account_id = ?
                                """,
                        Double.class,
                        accountId
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