package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.entities.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderRepository extends JpaRepository<Provider, String> {
    Optional<Provider> findByAccountId(UUID accountId);

}
