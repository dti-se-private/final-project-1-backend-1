package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WarehouseLedgerRepository extends JpaRepository<WarehouseLedger, UUID> {
//    List<WarehouseLedger> findByProductIdAndTimeBetween(UUID productId, OffsetDateTime startDate, OffsetDateTime endDate);
//
//    @Query("SELECT wl FROM WarehouseLedger wl WHERE wl.product.category.id = :categoryId AND wl.time BETWEEN :startTime AND :endTime")
//    List<WarehouseLedger> findByCategoryIdAndTimeBetween(@Param("categoryId") UUID categoryId,
//                                                         @Param("startTime") OffsetDateTime startTime,
//                                                         @Param("endTime") OffsetDateTime endTime);
//
//    List<WarehouseLedger> findByTimeBetween(OffsetDateTime startDate, OffsetDateTime endDate);
//
//    List<WarehouseLedger> findByProductIdInAndTimeBetween(List<UUID> productIds, OffsetDateTime startTime, OffsetDateTime endTime);

}
