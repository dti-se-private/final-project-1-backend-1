package org.dti.se.finalproject1backend1.outers.repositories.ones;

import org.dti.se.finalproject1backend1.inners.models.entities.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VerificationRepository extends JpaRepository<Verification, String>, JpaSpecificationExecutor<Verification> {
    Verification findByEmailAndCode(String email, String code);

    Verification findByEmail(String email);
}