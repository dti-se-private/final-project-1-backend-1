package org.dti.se.finalproject1backend1.inners.usecases.carts;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.carts.AddCartItemRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.carts.CartItemResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.carts.RemoveCartItemRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.products.ProductInsufficientException;
import org.dti.se.finalproject1backend1.outers.exceptions.products.ProductNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.CartCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.customs.ProductCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartUseCase {
    @Autowired
    CartCustomRepository cartCustomRepository;
    @Autowired
    ProductCustomRepository productCustomRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    AccountRepository accountRepository;

    public List<CartItemResponse> getCartItems(
            Account account,
            Integer page,
            Integer size,
            String search
    ) {
        Account foundAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        return cartCustomRepository.getCartItems(foundAccount, page, size, search);
    }

    public void addCartItem(Account account, AddCartItemRequest request) {
        Account foundAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);
        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(ProductNotFoundException::new);
        ProductResponse allWarehouseProduct = productCustomRepository.getAllWarehouseProduct(product.getId());

        if (allWarehouseProduct.getTotalQuantity() < request.getQuantity()) {
            throw new ProductInsufficientException();
        }

        cartCustomRepository.addCartItem(foundAccount.getId(), product.getId(), request.getQuantity());
    }

    public void removeCartItem(Account account, RemoveCartItemRequest request) {
        Account foundAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);
        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(ProductNotFoundException::new);

        cartCustomRepository.removeCartItem(foundAccount.getId(), product.getId(), request.getQuantity());
    }
}
