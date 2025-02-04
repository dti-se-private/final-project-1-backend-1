package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.AccountAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountAddressRepository extends JpaRepository<AccountAddress, UUID> {
    Optional<AccountAddress> findById(UUID id);
    Optional<AccountAddress> findByIdAndAccountId(UUID id, UUID accountId);
    Optional<List<AccountAddress>> findAllByAccountId(UUID accountId);
}