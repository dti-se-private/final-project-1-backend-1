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
    AccountAddressRepository accountAddressRepository;

    @Autowired
    AccountAddressCustomRepository accountAddressCustomRepository;

    @Autowired
    AccountRepository accountRepository;

    public AccountAddressResponse addAddress(Account account, AccountAddressRequest request) {
        Account foundAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        if (request.getIsPrimary().equals(true)) {
            setAllAddressesToNonPrimary(foundAccount);
        }

        AccountAddress foundAccountAddress = AccountAddress
                .builder()
                .id(UUID.randomUUID())
                .account(account)
                .name(request.getName())
                .address(request.getAddress())
                .isPrimary(request.getIsPrimary())
                .location(request.getLocation())
                .build();

        accountAddressRepository.saveAndFlush(foundAccountAddress);

        return AccountAddressResponse
                .builder()
                .id(foundAccountAddress.getId())
                .name(foundAccountAddress.getName())
                .address(foundAccountAddress.getAddress())
                .isPrimary(foundAccountAddress.getIsPrimary())
                .location(foundAccountAddress.getLocation())
                .build();
    }

    public AccountAddressResponse patchAddress(Account account, UUID addressId, AccountAddressRequest request) {
        Account foundAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        AccountAddress foundAccountAddress = accountAddressRepository
                .findById(addressId)
                .orElseThrow(AccountAddressNotFoundException::new);

        if (request.getIsPrimary()) {
            setAllAddressesToNonPrimary(foundAccount);
        }

        foundAccountAddress.setName(request.getName());
        foundAccountAddress.setAddress(request.getAddress());
        foundAccountAddress.setIsPrimary(request.getIsPrimary());
        foundAccountAddress.setLocation(request.getLocation());

        AccountAddress patchedAccountAddress = accountAddressRepository.saveAndFlush(foundAccountAddress);

        return AccountAddressResponse
                .builder()
                .id(patchedAccountAddress.getId())
                .name(patchedAccountAddress.getName())
                .address(patchedAccountAddress.getAddress())
                .isPrimary(patchedAccountAddress.getIsPrimary())
                .location(patchedAccountAddress.getLocation())
                .build();
    }

    public AccountAddressResponse getAddress(Account account, UUID addressId) {
        Account foundAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        AccountAddress foundAccountAddress = accountAddressRepository
                .findByIdAndAccountId(addressId, foundAccount.getId())
                .orElseThrow(AccountAddressNotFoundException::new);

        return AccountAddressResponse
                .builder()
                .id(foundAccountAddress.getId())
                .name(foundAccountAddress.getName())
                .address(foundAccountAddress.getAddress())
                .isPrimary(foundAccountAddress.getIsPrimary())
                .location(foundAccountAddress.getLocation())
                .build();
    }

    public List<AccountAddressResponse> getAddresses(Account account, Integer page, Integer size, String search) {
        Account foundAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        return accountAddressCustomRepository.getAccountAddresses(foundAccount, page, size, search);
    }

    public void deleteAddress(Account account, UUID addressId) {
        Account foundAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        AccountAddress foundAccountAddress = accountAddressRepository
                .findByIdAndAccountId(addressId, foundAccount.getId())
                .orElseThrow(AccountAddressNotFoundException::new);

        accountAddressRepository.delete(foundAccountAddress);
    }

    public void setAllAddressesToNonPrimary(Account account) {
        Account foundAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        List<AccountAddress> foundAccountAddresses = accountAddressRepository
                .findAllByAccountId(foundAccount.getId())
                .orElseThrow(AccountAddressNotFoundException::new);

        foundAccountAddresses.forEach(accountAddress -> accountAddress.setIsPrimary(false));

        accountAddressRepository.saveAllAndFlush(foundAccountAddresses);
    }
}
