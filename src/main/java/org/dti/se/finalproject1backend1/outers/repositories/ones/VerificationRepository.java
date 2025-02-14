package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, String>, JpaSpecificationExecutor<Verification> {
    Optional<Verification> findByEmailAndCodeAndType(String email, String code, String type);

    Optional<Verification> findByEmail(String email);

    Optional<Verification> findByEmailAndType(String email, String type);
}