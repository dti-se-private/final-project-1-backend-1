package org.dti.se.finalproject1backend1.inners.usecases.accounts;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountAddress;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountAddressRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountAddressResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountAddressNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.AccountAddressCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountAddressRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccountAddressUseCase {
    @Autowired
    private AccountAddressRepository accountAddressRepository;

    @Autowired
    private AccountAddressCustomRepository accountAddressCustomRepository;

    @Autowired
    private AccountRepository accountRepository;

    public AccountAddressResponse saveOne(Account account, AccountAddressRequest request) {
        Account findAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        if (request.getIsPrimary()) {
            setAllAddressesToNonPrimary(findAccount);
        }

        AccountAddress accountAddress = AccountAddress
                .builder()
                .id(UUID.randomUUID())
                .account(account)
                .name(request.getName())
                .address(request.getAddress())
                .isPrimary(request.getIsPrimary())
                .location(request.getLocation())
                .build();

        accountAddressRepository.saveAndFlush(accountAddress);

        return AccountAddressResponse
                .builder()
                .id(accountAddress.getId())
                .name(accountAddress.getName())
                .address(accountAddress.getAddress())
                .isPrimary(accountAddress.getIsPrimary())
                .location(accountAddress.getLocation())
                .build();
    }

    public AccountAddressResponse patchOneById(Account account, UUID addressId, AccountAddressRequest request) {
        Account findAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        AccountAddress patchedAccountAddress = accountAddressRepository
                .findById(addressId)
                .orElseThrow(AccountAddressNotFoundException::new);

        if (request.getIsPrimary()) {
            setAllAddressesToNonPrimary(findAccount);
        }

        patchedAccountAddress.setName(request.getName());
        patchedAccountAddress.setAddress(request.getAddress());
        patchedAccountAddress.setIsPrimary(request.getIsPrimary());
        patchedAccountAddress.setLocation(request.getLocation());

        accountAddressRepository.saveAndFlush(patchedAccountAddress);

        return AccountAddressResponse
                .builder()
                .id(patchedAccountAddress.getId())
                .name(patchedAccountAddress.getName())
                .address(patchedAccountAddress.getAddress())
                .isPrimary(patchedAccountAddress.getIsPrimary())
                .location(patchedAccountAddress.getLocation())
                .build();
    }

    public AccountAddressResponse findOneById(Account account, UUID addressId) {
        Account findAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        AccountAddress accountAddress = accountAddressRepository
                .findByIdAndAccountId(addressId, findAccount.getId())
                .orElseThrow(AccountAddressNotFoundException::new);

        return AccountAddressResponse
                .builder()
                .id(accountAddress.getId())
                .name(accountAddress.getName())
                .address(accountAddress.getAddress())
                .isPrimary(accountAddress.getIsPrimary())
                .location(accountAddress.getLocation())
                .build();
    }

    public List<AccountAddressResponse> findAll(Account account, Integer page, Integer size, List<String> filters, String search) {
        Account findAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        return accountAddressCustomRepository.getAllAccountAddresses(findAccount, page, size, filters, search);
    }

    public void deleteOneById(Account account, UUID addressId) {
        Account findAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        AccountAddress accountAddress = accountAddressRepository
                .findByIdAndAccountId(addressId, findAccount.getId())
                .orElseThrow(AccountAddressNotFoundException::new);

        accountAddressRepository.delete(accountAddress);
    }

    private void setAllAddressesToNonPrimary(Account account) {
        Account findAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        List<AccountAddress> accountAddresses = accountAddressRepository
                .findAllByAccountId(findAccount.getId())
                .orElseThrow(AccountAddressNotFoundException::new);

        accountAddresses.forEach(accountAddress -> accountAddress.setIsPrimary(false));

        accountAddressRepository.saveAllAndFlush(accountAddresses);
    }
}
