package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import jakarta.transaction.Transactional;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.entities.Verification;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterByEmailAndPasswordRequest;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountExistsException;
import org.dti.se.finalproject1backend1.outers.exceptions.authentications.OtpInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountPermissionRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class RegisterAuthenticationUseCase {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    VerificationRepository verificationRepository;

    @Autowired
    AccountPermissionRepository accountPermissionRepository;

    @Autowired
    SecurityConfiguration securityConfiguration;

    @Transactional
    public Account registerByEmailAndPassword(RegisterByEmailAndPasswordRequest request) {
        Account foundAccount = accountRepository.findFirstByEmail(request.getEmail());
        if (foundAccount != null) {
            throw new AccountExistsException();
        }

        String encodedPassword = securityConfiguration.encode(request.getPassword());
        Account accountToSave = Account
                .builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .phone(request.getPhone())
                .build();
        return accountRepository.save(accountToSave);
    }

    @Transactional
    public Account registerByInternal(RegisterByEmailAndPasswordRequest request) {
        Verification verification = verificationRepository.findByEmailAndCodeAndType(request.getEmail(), request.getOtp(), "REGISTER");
        if (verification == null || OffsetDateTime.now().isAfter(verification.getEndTime())) {
            throw new OtpInvalidException();
        }

        verificationRepository.delete(verification);

        Account foundAccount = accountRepository.findFirstByEmail(request.getEmail());
        if (foundAccount != null) {
            throw new AccountExistsException();
        }

        String encodedPassword = securityConfiguration.encode(request.getPassword());
        Account accountToSave = Account
                .builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .phone(request.getPhone())
                .isVerified(true)
                .provider("INTERNAL")
                .build();
        Account savedAccount = accountRepository.save(accountToSave);

        AccountPermission accountPermission = new AccountPermission();
        accountPermission.setId(UUID.randomUUID());
        accountPermission.setAccount(savedAccount);
        accountPermission.setPermission("CUSTOMER");
        accountPermissionRepository.save(accountPermission);

        verificationRepository.delete(verification);

        return savedAccount;
    }
}