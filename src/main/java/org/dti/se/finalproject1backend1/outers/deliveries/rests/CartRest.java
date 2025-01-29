package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.CartItem;
import org.dti.se.finalproject1backend1.inners.usecases.carts.CartUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartRest {

    private final CartUseCase cartService;

    @GetMapping("/{accountId}")
    public ResponseEntity<List<CartItem>> getCartItems(@PathVariable UUID accountId) {
        return ResponseEntity.ok(cartService.getCartItems(accountId));
    }

    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(
            @RequestParam UUID accountId,
            @RequestParam UUID productId,
            @RequestParam BigDecimal quantity) {
        return ResponseEntity.ok(cartService.addToCart(accountId, productId, quantity));
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable UUID cartItemId) {
        cartService.removeFromCart(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear/{accountId}")
    public ResponseEntity<Void> clearCart(@PathVariable UUID accountId) {
        cartService.clearCart(accountId);
        return ResponseEntity.noContent().build();
    }
}
