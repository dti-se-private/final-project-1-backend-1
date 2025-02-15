package org.dti.se.finalproject1backend1.inners.usecases.accounts;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;
import org.dti.se.finalproject1backend1.inners.usecases.authentications.VerificationUseCase;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
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
    private AccountRepository accountRepository;

    @Autowired
    private AccountCustomRepository accountCustomRepository;

    @Autowired
    SecurityConfiguration securityConfiguration;

    @Autowired
    VerificationUseCase verificationUseCase;

    public AccountResponse addAccount(AccountRequest request) {
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

        return getAccount(patchedAccount.getId());
    }

    public void deleteAccount(UUID id) {
        Account account = accountRepository
                .findById(id)
                .orElseThrow(AccountNotFoundException::new);
        accountRepository.delete(account);
    }

    public List<AccountResponse> getAdmins(Account account) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        return accountCustomRepository.getAdmins();
    }
}