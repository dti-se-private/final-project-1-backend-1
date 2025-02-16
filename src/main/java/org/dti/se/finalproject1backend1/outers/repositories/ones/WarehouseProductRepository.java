package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseProductRepository extends JpaRepository<WarehouseProduct, UUID> {
    // search by product name or warehouse
    @Query("SELECT wp FROM WarehouseProduct wp " +
            "WHERE LOWER(wp.product.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(wp.warehouse.name) LIKE LOWER(CONCAT('%', :search, '%')) ")
    Page<WarehouseProduct> searchByProductOrWarehouse(String search, Pageable pageable);

    // implements filtering logic
    @Query("SELECT wp FROM WarehouseProduct wp WHERE wp.quantity >0")
    Page<WarehouseProduct> findWithFilters(String filters, Pageable pageable);

    Optional<WarehouseProduct> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId);

//    // implement summation of quantity for product total quantity
//    @Query("SELECT wp.product.id, SUM(wp.quantity) " +
//            "FROM WarehouseProduct wp " +
//            "WHERE wp.product.id IN :productIds " +
//            "GROUP BY wp.product.id")
//    List<Object[]> findTotalQuantitiesByProductIds(@Param("productIds") List<UUID> productIds);

    // sum total of quantity
    @Query("SELECT COALESCE(SUM(wp.quantity), 0) FROM WarehouseProduct wp WHERE wp.product.id = :productId")
    Double sumQuantityByProductId(@Param("productId") UUID productId);
}
