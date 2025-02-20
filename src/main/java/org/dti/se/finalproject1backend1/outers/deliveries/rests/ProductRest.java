package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.dti.se.finalproject1backend1.inners.usecases.products.ProductUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.products.CategoryNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.products.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductRest {

    @Autowired
    ProductUseCase productUseCase;

    @GetMapping
    public ResponseEntity<ResponseBody<List<ProductResponse>>> getProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<ProductResponse> products = productUseCase.getProducts(page, size, search);
            return ResponseBody
                    .<List<ProductResponse>>builder()
                    .message("Products found.")
                    .data(products)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (Exception e) {
            return ResponseBody
                    .<List<ProductResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ResponseBody<ProductResponse>> getProduct(@PathVariable UUID productId) {
        try {
            ProductResponse product = productUseCase.getProduct(productId);
            return ResponseBody
                    .<ProductResponse>builder()
                    .message("Product found.")
                    .data(product)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (ProductNotFoundException e) {
            return ResponseBody
                    .<ProductResponse>builder()
                    .message("Product not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<ProductResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<ProductResponse>> addProduct(@RequestBody ProductRequest request) {
        try {
            ProductResponse product = productUseCase.addProduct(request);
            return ResponseBody
                    .<ProductResponse>builder()
                    .message("Product added.")
                    .data(product)
                    .build()
                    .toEntity(HttpStatus.CREATED);
        } catch (CategoryNotFoundException e) {
            return ResponseBody
                    .<ProductResponse>builder()
                    .message("Category not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<ProductResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{productId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<ProductResponse>> patchProduct(
            @PathVariable UUID productId,
            @RequestBody ProductRequest request
    ) {
        try {
            ProductResponse product = productUseCase.patchProduct(productId, request);
            return ResponseBody
                    .<ProductResponse>builder()
                    .message("Product patched.")
                    .data(product)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (CategoryNotFoundException e) {
            return ResponseBody
                    .<ProductResponse>builder()
                    .message("Category not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (ProductNotFoundException e) {
            return ResponseBody
                    .<ProductResponse>builder()
                    .message("Product not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<ProductResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<Void>> deleteProduct(@PathVariable UUID productId) {
        try {
            productUseCase.deleteProduct(productId);
            return ResponseBody
                    .<Void>builder()
                    .message("Product deleted.")
                    .build()
                    .toEntity(HttpStatus.OK);
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
