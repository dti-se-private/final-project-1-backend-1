package org.dti.se.finalproject1backend1.inners.usecases.accounts;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;
import org.dti.se.finalproject1backend1.inners.usecases.authentications.VerificationUseCase;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BasicAccountUseCase {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    SecurityConfiguration securityConfiguration;

    @Autowired
    VerificationUseCase verificationUseCase;

    public AccountResponse saveOne(AccountRequest request) {
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

        return AccountResponse
                .builder()
                .id(savedAccount.getId())
                .email(savedAccount.getEmail())
                .password(savedAccount.getPassword())
                .name(savedAccount.getName())
                .phone(savedAccount.getPhone())
                .isVerified(savedAccount.getIsVerified())
                .image(savedAccount.getImage())
                .build();
    }

    public AccountResponse findOneById(UUID id) {
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

    public Account findOneByEmail(String email) {
        return accountRepository
                .findByEmail(email)
                .orElseThrow(AccountNotFoundException::new);
    }

    public Account findOneByEmailAndPassword(String email, String password) {
        return accountRepository
                .findByEmailAndPassword(email, password)
                .orElseThrow(AccountNotFoundException::new);
    }

    public AccountResponse patchOneById(UUID id, AccountRequest request) {
        Account accountToPatch = accountRepository
                .findById(id)
                .orElseThrow(AccountNotFoundException::new);

        Boolean verifyResult = verificationUseCase.verifyOtp(request.getEmail(), request.getOtp(), "UPDATE_ACCOUNT");
        if (!verifyResult) {
            throw new VerificationInvalidException();
        }

        accountToPatch.setEmail(request.getEmail());
        accountToPatch.setName(request.getName());
        accountToPatch.setPhone(request.getPhone());
        accountToPatch.setImage(request.getImage());
        String encodedPassword = securityConfiguration.encode(request.getPassword());
        accountToPatch.setPassword(encodedPassword);

        Account patchedAccount = accountRepository.saveAndFlush(accountToPatch);

        return AccountResponse
                .builder()
                .id(patchedAccount.getId())
                .email(patchedAccount.getEmail())
                .password(patchedAccount.getPassword())
                .name(patchedAccount.getName())
                .phone(patchedAccount.getPhone())
                .isVerified(patchedAccount.getIsVerified())
                .image(patchedAccount.getImage())
                .build();
    }

    public void deleteOneById(UUID id) {
        Account account = accountRepository
                .findById(id)
                .orElseThrow(AccountNotFoundException::new);
        accountRepository.delete(account);
    }
}