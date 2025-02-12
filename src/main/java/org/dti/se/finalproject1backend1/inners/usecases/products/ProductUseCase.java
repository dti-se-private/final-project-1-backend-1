package org.dti.se.finalproject1backend1.inners.usecases.products;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.dti.se.finalproject1backend1.inners.usecases.categories.CategoryMapper;
import org.dti.se.finalproject1backend1.inners.usecases.categories.CategoryUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.products.ProductNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.ProductCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.ProductRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductUseCase {
    @Autowired
    private ProductCustomRepository productRepository;

    @Autowired
    private WarehouseProductRepository warehouseProductRepository;

    @Autowired
    private CategoryUseCase categoryService;


    public List<ProductResponse> getFilteredProducts(
            int page,
            int size,
            List<String> filters,
            String search
    ) {
        return productRepository.getProducts(page, size, filters, search);
    }

    public ProductResponse getProductById(UUID id) {
        try {
            return productRepository.getById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ProductNotFoundException();
        }
    }

    public ProductResponse addProduct(ProductResponse product) {
        // Verify category exists
        categoryService.getCategoryById(product.getCategory().getId());

        productRepository.create(product);
        return getProductById(product.getId());
    }

    public ProductResponse updateProduct(UUID id, ProductResponse product) {
        // Verify existence
        getProductById(id);
        // Verify category exists
        categoryService.getCategoryById(product.getCategory().getId());

        product.setId(id);
        productRepository.update(product);
        return getProductById(id);
    }

    public void deleteProduct(UUID id) {
        // Verify existence
        getProductById(id);
        productRepository.delete(id);
    }

}
