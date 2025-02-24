package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    @Query("SELECT wa.warehouse.id FROM WarehouseAdmin wa WHERE wa.account.id = :accountId")
    List<UUID> findWarehouseIdsByAccountId(@Param("accountId") UUID accountId);
}
