package org.dti.se.finalproject1backend1.inners.usecases.accounts;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountPermissionResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountPermissionRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountPermissionUseCase {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountPermissionRepository accountPermissionRepository;

    public AccountPermissionResponse getAccountPermissions(Account account) {
        Account foundAccount = accountRepository
                .findById(account.getId())
                .orElseThrow(AccountNotFoundException::new);

        List<AccountPermission> permissionsList = accountPermissionRepository
                .findByAccountId(account.getId())
                .orElseThrow(AccountPermissionNotFoundException::new);

        List<String> permissions = permissionsList
                .stream()
                .map(AccountPermission::getPermission)
                .collect(Collectors.toList());

        return AccountPermissionResponse
                .builder()
                .accountId(foundAccount.getId())
                .permissions(permissions)
                .build();
    }
}
