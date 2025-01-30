package org.dti.se.finalproject1backend1.inners.usecases.products;

import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductMapper {

    public ProductResponse toProductResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(product.getId());
        productResponse.setCategoryId(product.getCategory() != null ? product.getCategory().getId(): null);
        productResponse.setName(product.getName());
        productResponse.setDescription(product.getDescription());
        productResponse.setPrice(product.getPrice());
        productResponse.setImage(product.getImage());

        return productResponse;
    }

    // Maps ProductRequest DTO to Product entity
    public Product toProduct(ProductRequest productRequest, Category category) {
        Product product = new Product();
        product.setId(UUID.randomUUID()); // Generate a new ID for POST, use existing for PUT
        product.setCategory(category);
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setImage(productRequest.getImage());
        return product;
    }

    // Updates an existing Product entity using a ProductRequest
    public void updateProduct(Product product, ProductRequest productRequest, Category category) {
        product.setCategory(category);
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setImage(productRequest.getImage());
    }
}

