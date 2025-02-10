package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface WarehouseLedgerRepository extends JpaRepository<WarehouseLedger, UUID> {
    List<WarehouseLedger> findByProductIdAndTimeBetween(UUID productId, OffsetDateTime startDate, OffsetDateTime endDate);

    @Query("SELECT wl FROM WarehouseLedger wl WHERE wl.product.category.id = :categoryId AND wl.time BETWEEN :startTime AND :endTime")
    List<WarehouseLedger> findByCategoryIdAndTimeBetween(@Param("categoryId") UUID categoryId,
                                                         @Param("startTime") OffsetDateTime startTime,
                                                         @Param("endTime") OffsetDateTime endTime);

    List<WarehouseLedger> findByTimeBetween(OffsetDateTime startDate, OffsetDateTime endDate);

    List<WarehouseLedger> findByProductIdInAndTimeBetween(List<UUID> productIds, OffsetDateTime startTime, OffsetDateTime endTime);


//    @Query("""
//        SELECT SUM(wl.quantity), wl.time
//        FROM WarehouseLedger wl
//        JOIN wl.product p
//        JOIN p.category c
//        WHERE wl.warehouse.id IN :warehouseIds
//          AND c.id IN :categoryIds
//          AND p.id IN :productIds
//          AND wl.time BETWEEN :startTime AND :endTime
//        GROUP BY wl.time
//        ORDER BY wl.time ASC
//    """)
//    List<Object[]> getProductSales(
//            @Param("warehouseIds") Collection<UUID> warehouseIds,
//            @Param("categoryIds") Collection<UUID> categoryIds,
//            @Param("productIds") Collection<UUID> productIds,
//            @Param("startTime") OffsetDateTime startTime,
//            @Param("endTime") OffsetDateTime endTime
//    );


    // for product statistics
//    @Query("""
//    SELECT CAST(SUM(wl.postQuantity) AS double), wl.time
//    FROM WarehouseLedger wl
//    WHERE wl.warehouse.id IN :warehouseIds
//      AND wl.product.id IN :productIds
//      AND wl.time BETWEEN :startTime AND :endTime
//    GROUP BY wl.time
//    ORDER BY wl.time ASC
//""")
//    List<Object[]> getProductStockStatistics(
//            @Param("warehouseIds") List<UUID> warehouseIds,
//            @Param("productIds") List<UUID> productIds,
//            @Param("startTime") OffsetDateTime startTime,
//            @Param("endTime") OffsetDateTime endTime
//    );
}
