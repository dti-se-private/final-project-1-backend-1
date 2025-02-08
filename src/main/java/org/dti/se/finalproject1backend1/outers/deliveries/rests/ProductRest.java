package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.dti.se.finalproject1backend1.inners.usecases.categories.CategoryUseCase;
import org.dti.se.finalproject1backend1.inners.usecases.products.ProductMapper;
import org.dti.se.finalproject1backend1.inners.usecases.products.ProductUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductRest {
    @Autowired
    private ProductUseCase productService;

    @Autowired
    private CategoryUseCase categoryService;

    @Autowired
    private ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<ProductResponse> response = productService.getFilteredProducts(pageable, category, search);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable UUID id) {
        Product product = productService.getProductById(id);
        return productMapper.toProductResponse(product);
    }

    @PostMapping
    public ProductResponse addProduct(@RequestBody ProductRequest productRequest) {
        Category category = categoryService.getCategoryById(productRequest.getCategoryId());

        Product product = productMapper.toProduct(productRequest, category);
        productService.addProduct(product);

        ProductResponse productResponse = productMapper.toProductResponse(product);
        return productResponse;
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable UUID id, @RequestBody ProductRequest productRequest) {
        Product existingProduct = productService.getProductById(id);

        Category category = categoryService.getCategoryById(productRequest.getCategoryId());

        productMapper.updateProduct(existingProduct, productRequest, category);
        productService.updateProduct(id, existingProduct);
        ProductResponse productResponse = productMapper.toProductResponse(existingProduct);
        return productResponse;
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(UUID id) {
        productService.deleteProduct(id);
    }
}