package org.dti.se.finalproject1backend1.inners.usecases.accounts;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.Provider;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;
import org.dti.se.finalproject1backend1.inners.usecases.authentications.VerificationUseCase;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.blobs.ObjectSizeExceededException;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.AccountCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BasicAccountUseCase {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountCustomRepository accountCustomRepository;

    @Autowired
    SecurityConfiguration securityConfiguration;

    @Autowired
    VerificationUseCase verificationUseCase;

    public AccountResponse addAccount(AccountRequest request) {
        if (request.getImage() != null && request.getImage().length > 1024000) {
            throw new ObjectSizeExceededException();
        }

        String encodedPassword = securityConfiguration.encode(request.getPassword());
        Account account = Account
                .builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .isVerified(false)
                .image(request.getImage())
                .build();
        Account savedAccount = accountRepository.saveAndFlush(account);

        return getAccount(savedAccount.getId());
    }

    public AccountResponse getAccount(UUID id) {
        Account foundAccount = accountRepository
                .findById(id)
                .orElseThrow(AccountNotFoundException::new);

        return AccountResponse
                .builder()
                .id(foundAccount.getId())
                .email(foundAccount.getEmail())
                .password(foundAccount.getPassword())
                .name(foundAccount.getName())
                .phone(foundAccount.getPhone())
                .isVerified(foundAccount.getIsVerified())
                .image(foundAccount.getImage())
                .build();
    }

    public AccountResponse patchAccount(UUID id, AccountRequest request) {
        Boolean isOtpVerified = verificationUseCase
                .verifyOtp(request.getEmail(), request.getOtp(), "UPDATE_ACCOUNT");

        if (!isOtpVerified) {
            throw new VerificationInvalidException();
        }

        if (request.getImage() != null && request.getImage().length > 1024000) {
            throw new ObjectSizeExceededException();
        }

        Account accountToPatch = accountRepository
                .findById(id)
                .orElseThrow(AccountNotFoundException::new);

        List<Provider> providers = accountToPatch
                .getProviders()
                .stream()
                .toList();

        Boolean isProviderContainInternal = providers
                .stream()
                .anyMatch(provider -> provider.getName().equals("INTERNAL"));

        if (isProviderContainInternal) {
            accountToPatch.setEmail(request.getEmail());
            String encodedPassword = securityConfiguration.encode(request.getPassword());
            accountToPatch.setPassword(encodedPassword);
        }

        accountToPatch.setName(request.getName());
        accountToPatch.setPhone(request.getPhone());
        accountToPatch.setImage(request.getImage());

        Account patchedAccount = accountRepository.saveAndFlush(accountToPatch);

        return getAccount(patchedAccount.getId());
    }

    public void deleteAccount(UUID id) {
        Account account = accountRepository
                .findById(id)
                .orElseThrow(AccountNotFoundException::new);
        accountRepository.delete(account);
    }

    public List<AccountResponse> getAdmins(
            Integer page,
            Integer size,
            String search
    ) {
        return accountCustomRepository.getAdmins(page, size, search);
    }
}