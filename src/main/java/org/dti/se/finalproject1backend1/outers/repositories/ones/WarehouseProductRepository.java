package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseProductRepository extends JpaRepository<WarehouseProduct, UUID> {
    // search by product name or warehouse
    @Query("SELECT wp FROM WarehouseProduct wp " +
            "WHERE LOWER(wp.product.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(wp.warehouse.name) LIKE LOWER(CONCAT('%', :search, '%')) ")
    Page<WarehouseProduct> searchByProductOrWarehouse(String search, Pageable pageable);

    // implements filtering logic
    @Query("SELECT wp FROM WarehouseProduct wp WHERE wp.quantity >0")
    Page<WarehouseProduct> findWithFilters(String filters, Pageable pageable);

    // method to update the quantity in the warehouse
    @Query("SELECT wp FROM WarehouseProduct wp WHERE wp.product.id = :productId AND wp.warehouse.id = :warehouseId")
    Optional<WarehouseProduct> findByProductAndWarehouse(@Param("productId") UUID productId, @Param("warehouseId") UUID warehouseId);
}
