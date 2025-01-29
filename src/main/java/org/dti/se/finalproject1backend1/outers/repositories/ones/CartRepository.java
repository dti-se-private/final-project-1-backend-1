package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByAccount(Account account);
    Optional<CartItem> findByAccountAndProductId(Account account, UUID productId);
}
