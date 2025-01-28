package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.transaction.Transactional;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.entities.Provider;
import org.dti.se.finalproject1backend1.inners.models.entities.Verification;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterByEmailAndPasswordRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterByExternalRequest;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountExistsException;
import org.dti.se.finalproject1backend1.outers.exceptions.authentications.OtpInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountPermissionRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.ProviderRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
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
    ProviderRepository providerRepository;

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
                .build();
        Account savedAccount = accountRepository.save(accountToSave);

        Provider accountProvider = new Provider();
        accountProvider.setId(UUID.randomUUID());
        accountProvider.setAccount(savedAccount);
        accountProvider.setName("INTERNAL");
        providerRepository.save(accountProvider);

        AccountPermission accountPermission = new AccountPermission();
        accountPermission.setId(UUID.randomUUID());
        accountPermission.setAccount(savedAccount);
        accountPermission.setPermission("CUSTOMER");
        accountPermissionRepository.save(accountPermission);

        verificationRepository.delete(verification);

        return savedAccount;
    }

    @Transactional
    public Account registerByExternal(RegisterByExternalRequest request) {
        try {
            String idTokenString = request.getIdToken();
            if (idTokenString == null || idTokenString.isEmpty()) {
                throw new RuntimeException("ID token is null or empty");
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(System.getenv("GOOGLE_CLIENT_ID")))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) {
                throw new RuntimeException("Invalid ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String profile = (String) payload.get("picture");

            Account foundAccount = accountRepository.findFirstByEmail(email);
            if (foundAccount != null) {
                throw new AccountExistsException();
            }

            Account accountToSave = Account
                    .builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .email(email)
                    .isVerified(true)
                    .image(profile.getBytes())
                    .build();
            Account savedAccount = accountRepository.save(accountToSave);

            Provider accountProvider = new Provider();
            accountProvider.setId(UUID.randomUUID());
            accountProvider.setAccount(savedAccount);
            accountProvider.setName("EXTERNAL");
            providerRepository.save(accountProvider);

            AccountPermission accountPermission = new AccountPermission();
            accountPermission.setId(UUID.randomUUID());
            accountPermission.setAccount(savedAccount);
            accountPermission.setPermission("CUSTOMER");
            accountPermissionRepository.save(accountPermission);

            return savedAccount;
        } catch (Exception e) {
            throw new RuntimeException("Error verifying Google ID token", e);
        }
    }
}