package org.dti.se.finalproject1backend1.inners.usecases.accounts;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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
        account.setPassword(encodedPassword);
        return accountRepository.save(account);
    }

    public Account findOneById(UUID id) {
        Account account;
        try {
            account = accountRepository.findFirstById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new AccountNotFoundException();
        }
        return account;
    }

    public Account findOneByEmail(String email) {
        Account account;
        try {
            account = accountRepository.findFirstByEmail(email);
        } catch (EmptyResultDataAccessException e) {
            throw new AccountNotFoundException();
        }
        return account;
    }

    public Account findOneByEmailAndPassword(String email, String password) {
        Account account;
        try {
            account = accountRepository.findFirstByEmailAndPassword(email, password);
        } catch (EmptyResultDataAccessException e) {
            throw new AccountNotFoundException();
        }
        return account;
    }

    public Account patchOneById(UUID id, Account account) {
        Account accountToPatch;
        try {
            accountToPatch = accountRepository.findFirstById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new AccountNotFoundException();
        }
        accountToPatch.patchFrom(account).setIsNew(false);
        String encodedPassword = securityConfiguration.encode(accountToPatch.getPassword());
        accountToPatch.setPassword(encodedPassword);
        return accountRepository.save(accountToPatch);
    }

    public void deleteOneById(UUID id) {
        Account account;
        try {
            account = accountRepository.findFirstById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new AccountNotFoundException();
        }
        accountRepository.delete(account);
    }
}