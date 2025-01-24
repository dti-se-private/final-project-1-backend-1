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
        Account account = accountRepository.findFirstById(id);
        if (account == null) {
            throw new AccountNotFoundException();
        }
        return account;
    }

    public Account findOneByEmail(String email) {
        Account account = accountRepository.findFirstByEmail(email);
        if (account == null) {
            throw new AccountNotFoundException();
        }
        return account;
    }

    public Account findOneByEmailAndPassword(String email, String password) {
        Account account = accountRepository.findFirstByEmailAndPassword(email, password);
        if (account == null) {
            throw new AccountNotFoundException();
        }
        return account;
    }

    public Account patchOneById(UUID id, Account account) {
        Account accountToPatch = accountRepository.findFirstById(id);
        if (accountToPatch == null) {
            throw new AccountNotFoundException();
        }
        accountToPatch.patchFrom(account);
        String encodedPassword = securityConfiguration.encode(accountToPatch.getPassword());
        accountToPatch.setPassword(encodedPassword);
        return accountRepository.save(accountToPatch);
    }

    public void deleteOneById(UUID id) {
        Account account = accountRepository.findFirstById(id);
        if (account == null) {
            throw new AccountNotFoundException();
        }
        accountRepository.delete(account);
    }
}