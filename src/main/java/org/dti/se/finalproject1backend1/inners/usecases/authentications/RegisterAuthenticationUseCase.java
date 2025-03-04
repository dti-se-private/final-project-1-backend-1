package org.dti.se.finalproject1backend1.inners.usecases.authentications;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.entities.Provider;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterByExternalRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterByInternalRequest;
import org.dti.se.finalproject1backend1.outers.configurations.GoogleConfiguration;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountExistsException;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountPermissionRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
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
    VerificationUseCase verificationUseCase;

    @Autowired
    GoogleConfiguration googleConfiguration;


    public AccountResponse registerByInternal(RegisterByInternalRequest request) {
        boolean isOtpVerified = verificationUseCase.verifyOtp(request.getEmail(), request.getOtp(), "REGISTER");

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

        return AccountResponse
                .builder()
                .id(savedAccount.getId())
                .name(savedAccount.getName())
                .email(savedAccount.getEmail())
                .password(savedAccount.getPassword())
                .phone(savedAccount.getPhone())
                .isVerified(savedAccount.getIsVerified())
                .image(savedAccount.getImage())
                .build();
    }


    public AccountResponse registerByExternal(RegisterByExternalRequest request) {
        GoogleTokenResponse tokenResponse;
        try {
            tokenResponse = googleConfiguration.getToken(request.getAuthorizationCode());
        } catch (GeneralSecurityException | IOException e) {
            throw new VerificationInvalidException();
        }

        GoogleIdToken idToken;
        try {
            idToken = tokenResponse.parseIdToken();
        } catch (IOException e) {
            throw new VerificationInvalidException();
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = payload.get("name").toString();
        String pictureUrl = payload.get("picture").toString();

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
                .image(GoogleConfiguration.convertUrlToHexByte(pictureUrl))
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

        return AccountResponse
                .builder()
                .id(savedAccount.getId())
                .name(savedAccount.getName())
                .email(savedAccount.getEmail())
                .password(savedAccount.getPassword())
                .phone(savedAccount.getPhone())
                .isVerified(savedAccount.getIsVerified())
                .image(savedAccount.getImage())
                .build();
    }
}