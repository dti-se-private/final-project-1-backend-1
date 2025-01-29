package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    @Query(value = "SELECT * FROM warehouses w " +
            "WHERE w.id != :requesterWarehouseId " +
            "AND EXISTS (SELECT 1 FROM warehouse_products wp " +
            "            WHERE wp.warehouse_id = w.id " +
            "            AND wp.product_id = :productId " +
            "            AND wp.quantity >= :requiredQuantity) " +
            "LIMIT 1",
            nativeQuery = true)
    Optional<Warehouse> findNearestWarehouseWithStock(
            @Param("productId") UUID productId,
            @Param("requesterWarehouseId") UUID requesterWarehouseId,
            @Param("requiredQuantity") BigDecimal requiredQuantity);
}
