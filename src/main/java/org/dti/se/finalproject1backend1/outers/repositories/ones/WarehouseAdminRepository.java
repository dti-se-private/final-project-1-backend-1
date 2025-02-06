package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseAdminRepository extends JpaRepository<WarehouseAdmin, UUID> {
    Optional<WarehouseAdmin> findById(UUID id);
    boolean existsByAccountIdAndWarehouseId(UUID accountId, UUID warehouseId);
}
