package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.outers.repositories.ones.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductRest {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
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