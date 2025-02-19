package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseAdminRepository extends JpaRepository<WarehouseAdmin, UUID> {
    Optional<WarehouseAdmin> findById(UUID id);

    Optional<WarehouseAdmin> findByAccount(Account account);

    boolean existsByAccountIdAndWarehouseId(UUID accountId, UUID warehouseId);
}
