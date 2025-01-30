package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

}
