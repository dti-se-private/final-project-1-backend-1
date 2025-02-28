package org.dti.se.finalproject1backend1.inners.usecases.products;

import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.blobs.ObjectSizeExceededException;
import org.dti.se.finalproject1backend1.outers.exceptions.products.CategoryNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.products.ProductNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.ProductCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.CategoryRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Service
public class ProductUseCase {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductCustomRepository productCustomRepository;


    public List<ProductResponse> getProducts(
            Integer page,
            Integer size,
            String search
    ) {
        return productCustomRepository.getProducts(page, size, search);
    }

    public ProductResponse getProduct(@PathVariable UUID productId) {
        Product foundProduct = productRepository
                .findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        return productCustomRepository.getProduct(foundProduct.getId());
    }

    public ProductResponse addProduct(@RequestBody ProductRequest request) {
        if (request.getImage() != null && request.getImage().length > 1024000) {
            throw new ObjectSizeExceededException();
        }

        Category foundCategory = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(CategoryNotFoundException::new);

        Product newProduct = Product
                .builder()
                .id(UUID.randomUUID())
                .category(foundCategory)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .image(request.getImage())
                .build();

        productRepository.saveAndFlush(newProduct);

        return getProduct(newProduct.getId());
    }

    public ProductResponse patchProduct(
            @PathVariable UUID id,
            @RequestBody ProductRequest request
    ) {
        if (request.getImage() != null && request.getImage().length > 1024000) {
            throw new ObjectSizeExceededException();
        }

        Product foundProduct = productRepository
                .findById(id)
                .orElseThrow(ProductNotFoundException::new);

        Category foundCategory = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(CategoryNotFoundException::new);

        foundProduct.setCategory(foundCategory);
        foundProduct.setName(request.getName());
        foundProduct.setDescription(request.getDescription());
        foundProduct.setPrice(request.getPrice());
        foundProduct.setImage(request.getImage());

        productRepository.saveAndFlush(foundProduct);

        return getProduct(foundProduct.getId());
    }

    public void deleteProduct(@PathVariable UUID productId) {
        Product foundProduct = productRepository
                .findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        productRepository.delete(foundProduct);
    }
}
