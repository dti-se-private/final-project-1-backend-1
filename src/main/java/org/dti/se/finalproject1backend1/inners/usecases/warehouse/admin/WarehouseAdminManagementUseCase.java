package org.dti.se.finalproject1backend1.inners.usecases.warehouse.admin;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseAdmin;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.admin.WarehouseAdminRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.admin.WarehouseAdminResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseAdminAndWarehouseDuplicateException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseAdminNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.WarehouseAdminCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountPermissionRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseAdminRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WarehouseAdminManagementUseCase {

    @Autowired
    private WarehouseAdminCustomRepository warehouseCustomRepository;

    @Autowired
    private WarehouseAdminRepository warehouseAdminRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private AccountPermissionRepository accountPermissionRepository;

    public List<WarehouseAdminResponse> getAllWarehouseAdmins(
            Account account,
            Integer page,
            Integer size,
            List<String> filters,
            String search
    ) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        return warehouseCustomRepository.getAllWarehouseAdmins(page, size, filters, search);
    }

    public WarehouseAdminResponse getWarehouseAdmin(Account account, UUID warehouseAdminId) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        WarehouseAdmin warehouseAdmin = warehouseAdminRepository
                .findById(warehouseAdminId)
                .orElseThrow(WarehouseAdminNotFoundException::new);

        return WarehouseAdminResponse
                .builder()
                .id(warehouseAdmin.getId())
                .accountId(warehouseAdmin.getAccount().getId())
                .warehouseId(warehouseAdmin.getWarehouse().getId())
                .build();
    }

    public WarehouseAdminResponse assignWarehouseAdmin(Account account, WarehouseAdminRequest warehouseAdminRequest) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        Account adminAccount = accountRepository
                .findById(warehouseAdminRequest.getAccountId())
                .orElseThrow(AccountNotFoundException::new);

        Warehouse warehouse = warehouseRepository
                .findById(warehouseAdminRequest.getWarehouseId())
                .orElseThrow(WarehouseNotFoundException::new);

        boolean exists = warehouseAdminRepository
                .existsByAccountIdAndWarehouseId(warehouseAdminRequest.getAccountId(), warehouseAdminRequest.getWarehouseId());
        if (exists) {
            throw new WarehouseAdminAndWarehouseDuplicateException();
        }

        WarehouseAdmin warehouseAdmin = new WarehouseAdmin();
        warehouseAdmin.setId(UUID.randomUUID());
        warehouseAdmin.setAccount(adminAccount);
        warehouseAdmin.setWarehouse(warehouse);

        warehouseAdminRepository.saveAndFlush(warehouseAdmin);

        return WarehouseAdminResponse
                .builder()
                .id(warehouseAdmin.getId())
                .accountId(warehouseAdmin.getAccount().getId())
                .warehouseId(warehouseAdmin.getWarehouse().getId())
                .build();
    }

    public WarehouseAdminResponse updateWarehouseAdmin(Account account, UUID id, WarehouseAdminRequest warehouseAdminRequest) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        WarehouseAdmin warehouseAdmin = warehouseAdminRepository
                .findById(id)
                .orElseThrow(WarehouseAdminNotFoundException::new);

        Account adminAccount = accountRepository
                .findById(warehouseAdminRequest.getAccountId())
                .orElseThrow(AccountNotFoundException::new);

        Warehouse warehouse = warehouseRepository
                .findById(warehouseAdminRequest.getWarehouseId())
                .orElseThrow(WarehouseNotFoundException::new);

        warehouseAdmin.setAccount(adminAccount);
        warehouseAdmin.setWarehouse(warehouse);

        warehouseAdminRepository.saveAndFlush(warehouseAdmin);

        return WarehouseAdminResponse
                .builder()
                .id(warehouseAdmin.getId())
                .accountId(warehouseAdmin.getAccount().getId())
                .warehouseId(warehouseAdmin.getWarehouse().getId())
                .build();
    }

    public void deleteWarehouseAdmin(Account account, UUID id) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        WarehouseAdmin warehouseAdmin = warehouseAdminRepository
                .findById(id)
                .orElseThrow(WarehouseAdminNotFoundException::new);

        warehouseAdminRepository.delete(warehouseAdmin);
    }
}
