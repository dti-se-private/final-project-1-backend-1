package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.carts.AddCartItemRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.carts.CartItemResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.carts.RemoveCartItemRequest;
import org.dti.se.finalproject1backend1.inners.usecases.carts.CartUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.products.ProductInsufficientException;
import org.dti.se.finalproject1backend1.outers.exceptions.products.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartRest {

    @Autowired
    CartUseCase cartUseCase;

    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<List<CartItemResponse>>> getCartItems(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<CartItemResponse> cartItems = cartUseCase
                    .getCartItems(account, page, size, search);
            return ResponseBody
                    .<List<CartItemResponse>>builder()
                    .message("Cart items found.")
                    .data(cartItems)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<List<CartItemResponse>>builder()
                    .message("Account not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<List<CartItemResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<Void>> addCartItem(
            @AuthenticationPrincipal Account account,
            @RequestBody AddCartItemRequest request
    ) {
        try {
            cartUseCase.addCartItem(account, request);
            return ResponseBody
                    .<Void>builder()
                    .message("Item added to cart.")
                    .data(null)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Account not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (ProductNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Product not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (ProductInsufficientException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Product insufficient.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/remove")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN', 'CUSTOMER')")
    public ResponseEntity<ResponseBody<Void>> removeCartItem(
            @AuthenticationPrincipal Account account,
            @RequestBody RemoveCartItemRequest request
    ) {
        try {
            cartUseCase.removeCartItem(account, request);
            return ResponseBody
                    .<Void>builder()
                    .message("Item removed from cart.")
                    .data(null)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Account not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (ProductNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Product not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
