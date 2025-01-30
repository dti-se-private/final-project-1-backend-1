package org.dti.se.finalproject1backend1.inners.usecases.carts;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.CartItem;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.CartRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartUseCase {

    private final CartRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    public List<CartItem> getCartItems(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return cartItemRepository.findByAccount(account);
    }

    @Transactional
    public CartItem addToCart(UUID accountId, UUID productId, BigDecimal quantity) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        Optional<CartItem> existingCartItem = cartItemRepository.findByAccountAndProductId(account, productId);
        CartItem cartItem = existingCartItem.orElseGet(() -> {
            CartItem newItem = new CartItem();
            newItem.setId(UUID.randomUUID());
            newItem.setAccount(account);
            newItem.setProduct(product);
            newItem.setQuantity(BigDecimal.ZERO);
            return newItem;
        });

        cartItem.setQuantity(cartItem.getQuantity().add(quantity));
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(UUID cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        List<CartItem> cartItems = cartItemRepository.findByAccount(account);
        cartItemRepository.deleteAll(cartItems);
    }
}
