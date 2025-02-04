package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.entities.Provider;
import org.dti.se.finalproject1backend1.inners.models.entities.Verification;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterByEmailAndPasswordRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterAndLoginByExternalRequest;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountExistsException;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationExpiredException;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountPermissionRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.ProviderRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegisterAuthenticationUseCase {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountPermissionRepository accountPermissionRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    SecurityConfiguration securityConfiguration;

    @Autowired
    GoogleIdTokenVerifier googleIdTokenVerifier;

    @Autowired
    OtpUseCase otpUseCase;


    public Account registerByEmailAndPassword(RegisterByEmailAndPasswordRequest request) {
        Optional<Account> foundAccount = accountRepository
                .findByEmail(request.getEmail());

        if (foundAccount.isPresent()) {
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
        return accountRepository.saveAndFlush(accountToSave);
    }


    public Account registerByInternal(RegisterByEmailAndPasswordRequest request) {
        boolean isOtpVerified = otpUseCase.verifyOtp(request.getEmail(), request.getOtp(), "REGISTER");

        if (!isOtpVerified) {
            throw new VerificationNotFoundException();
        }

        Optional<Account> foundAccount = accountRepository
                .findByEmail(request.getEmail());

        if (foundAccount.isPresent()) {
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
        Account savedAccount = accountRepository.saveAndFlush(accountToSave);

        Provider accountProvider = new Provider();
        accountProvider.setId(UUID.randomUUID());
        accountProvider.setAccount(savedAccount);
        accountProvider.setName("INTERNAL");
        providerRepository.saveAndFlush(accountProvider);

        AccountPermission accountPermission = new AccountPermission();
        accountPermission.setId(UUID.randomUUID());
        accountPermission.setAccount(savedAccount);
        accountPermission.setPermission("CUSTOMER");
        accountPermissionRepository.saveAndFlush(accountPermission);

        return savedAccount;
    }


    public Account registerByExternal(RegisterAndLoginByExternalRequest request) {
        GoogleIdToken idToken;

        String idTokenString = request.getIdToken();
        if (idTokenString == null || idTokenString.isEmpty()) {
            throw new VerificationNotFoundException("ID token is null or empty");
        }

        try {
            idToken = googleIdTokenVerifier.verify(request.getIdToken());
        } catch (GeneralSecurityException | IOException e) {
            throw new VerificationInvalidException("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = payload.get("name").toString();
        String picture = payload.get("picture").toString();

        Optional<Account> foundAccount = accountRepository
                .findByEmail(email);

        if (foundAccount.isPresent()) {
            throw new AccountExistsException();
        }

        Account accountToSave = Account
                .builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email)
                .isVerified(true)
                .image(picture.getBytes())
                .build();
        Account savedAccount = accountRepository.saveAndFlush(accountToSave);

        Provider accountProvider = new Provider();
        accountProvider.setId(UUID.randomUUID());
        accountProvider.setAccount(savedAccount);
        accountProvider.setName("EXTERNAL");
        providerRepository.saveAndFlush(accountProvider);

        AccountPermission accountPermission = new AccountPermission();
        accountPermission.setId(UUID.randomUUID());
        accountPermission.setAccount(savedAccount);
        accountPermission.setPermission("CUSTOMER");
        accountPermissionRepository.saveAndFlush(accountPermission);

        return savedAccount;

    }
}