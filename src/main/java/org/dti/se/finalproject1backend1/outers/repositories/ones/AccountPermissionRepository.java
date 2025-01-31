package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountPermissionRepository extends JpaRepository<AccountPermission, UUID>, JpaSpecificationExecutor<AccountPermission> {
    Optional<List<AccountPermission>> findByAccountId(UUID accountId);
}