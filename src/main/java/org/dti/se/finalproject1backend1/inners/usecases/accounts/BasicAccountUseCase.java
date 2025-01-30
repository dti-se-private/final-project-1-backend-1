package org.dti.se.finalproject1backend1.inners.usecases.accounts;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
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

    public Account saveOne(Account account) {
        String encodedPassword = securityConfiguration.encode(account.getPassword());
        account.setId(UUID.randomUUID());
        account.setPassword(encodedPassword);
        return accountRepository.save(account);
    }

    public Account findOneById(UUID id) {
        return accountRepository
                .findById(id)
                .orElseThrow(AccountNotFoundException::new);
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

    public Account patchOneById(UUID id, Account account) {
        Account accountToPatch = accountRepository
                .findById(id)
                .orElseThrow(AccountNotFoundException::new);
        accountToPatch.patchFrom(account);
        String encodedPassword = securityConfiguration.encode(accountToPatch.getPassword());
        accountToPatch.setPassword(encodedPassword);
        return accountRepository.save(accountToPatch);
    }

    public void deleteOneById(UUID id) {
        Account account = accountRepository
                .findById(id)
                .orElseThrow(AccountNotFoundException::new);
        accountRepository.delete(account);
    }
}