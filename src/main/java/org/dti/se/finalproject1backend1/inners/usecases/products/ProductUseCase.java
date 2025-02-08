package org.dti.se.finalproject1backend1.inners.usecases.products;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.dti.se.finalproject1backend1.outers.repositories.ones.ProductRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ProductRepository productRepository;

    @Autowired
    private WarehouseProductRepository warehouseProductRepository;

//    public List<Product> getAllProducts() {
//        return productRepository.findAll();
//    }
//    public Page<ProductResponse> getAllProducts(Pageable pageable, String category, String search) {
//        Specification<Product> spec = Specification.where(null);
//
//        // add category filter
//        if (StringUtils.hasText(category)) {
//            spec = spec.and((root, query, cb) -> {
//                        Join<Product, Category> categoryJoin = root.join("category");
//                        return cb.equal(cb.lower(categoryJoin.get("name")), category.toLowerCase());
//                    });
//        }
//
//        // add search filter (name or description)
//        if (StringUtils.hasText(search)) {
//            String searchPattern = "%" + search.toLowerCase() + "%";
//            spec = spec.and((root, query, cb) ->
//                    cb.or(
//                            cb.like(cb.lower(root.get("name")),  searchPattern),
//                            cb.like(cb.lower(root.get("description")), searchPattern)
//                    )
//            );
//        }
//
//        //execute query with pagination
//        Page<Product> products = productRepository.findAll(spec, pageable);
//
//
//        // Map to DTO
//        return products.map(product ->
//                new ProductResponse(
//                        product.getId(),
//
//                )
//        );
//    }
    // how to get Product total quantity for filtered product
    public Page<ProductResponse> getFilteredProducts(Pageable pageable, String categoryName, String searchTerm) {
        // 1. First get filtered products using existing specifications
        Specification<Product> spec = buildSpecifications(categoryName, searchTerm);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        // 2. Get quantities in bulk for all products in this page
        List<UUID> productIds = productPage.getContent()
                .stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        Map<UUID, Double> quantityMap = warehouseProductRepository
                .findTotalQuantitiesByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (UUID) arr[0],
                        arr -> (Double) arr[1]
                ));

        // 3. Map to ProductResponse with quantities
        List<ProductResponse> responses = productPage.getContent()
                .stream()
                .map(product -> mapToResponse(product, quantityMap))
                .collect(Collectors.toList());

        // 4. Return paginated result
        return new PageImpl<>(responses, pageable, productPage.getTotalElements());
    }

    private ProductResponse mapToResponse(Product product, Map<UUID, Double> quantityMap) {
        return new ProductResponse()
                .setId(product.getId())
                .setName(product.getName())
                .setDescription(product.getDescription())
                .setPrice(product.getPrice())
                .setImage(product.getImage())
                .setTotalQuantity(quantityMap.getOrDefault(product.getId(), 0.0))
                .setCategory(mapCategory(product.getCategory()));
    }

    private CategoryResponse mapCategory(Category category) {
        return new CategoryResponse()
                .setId(category.getId())
                .setName(category.getName())
                .setDescription(category.getDescription());
    }

    private Specification<Product> buildSpecifications(String categoryName, String searchTerm) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Category filter
            if (StringUtils.hasText(categoryName)) {
                Join<Product, Category> categoryJoin = root.join("category");
                predicates.add(cb.equal(cb.lower(categoryJoin.get("name")), categoryName.toLowerCase()));
            }

            // Search filter
            if (StringUtils.hasText(searchTerm)) {
                String pattern = "%" + searchTerm.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not Found for ID: " + id));
    }

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(UUID id, Product product) {
        product.setId(id);
        return productRepository.save(product);
    }

    public void deleteProduct(UUID id) {
        productRepository.deleteById(id);
    }

}
