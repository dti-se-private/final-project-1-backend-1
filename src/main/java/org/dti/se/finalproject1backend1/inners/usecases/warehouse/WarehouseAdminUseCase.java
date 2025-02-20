package org.dti.se.finalproject1backend1.inners.usecases.warehouse;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseAdmin;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseadmins.WarehouseAdminRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseadmins.WarehouseAdminResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseAdminExistsException;
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
public class WarehouseAdminUseCase {

    @Autowired
    WarehouseAdminCustomRepository warehouseCustomRepository;

    @Autowired
    WarehouseAdminRepository warehouseAdminRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    WarehouseRepository warehouseRepository;

    @Autowired
    AccountPermissionRepository accountPermissionRepository;

    public List<WarehouseAdminResponse> getWarehouseAdmins(
            Integer page,
            Integer size,
            String search
    ) {
        return warehouseCustomRepository.getWarehouseAdmins(page, size, search);
    }

    public WarehouseAdminResponse getWarehouseAdmin(UUID warehouseAdminId) {
        return warehouseCustomRepository.getWarehouseAdmin(warehouseAdminId);
    }

    public WarehouseAdminResponse addWarehouseAdmin(WarehouseAdminRequest warehouseAdminRequest) {
        Account account = accountRepository
                .findById(warehouseAdminRequest.getAccountId())
                .orElseThrow(AccountNotFoundException::new);

        Warehouse warehouse = warehouseRepository
                .findById(warehouseAdminRequest.getWarehouseId())
                .orElseThrow(WarehouseNotFoundException::new);

        Boolean isAccountAnAdmin = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .anyMatch(permission -> permission.equals("SUPER_ADMIN") || permission.equals("WAREHOUSE_ADMIN"));
        if (!isAccountAnAdmin) {
            throw new AccountPermissionInvalidException();
        }

        Boolean isExists = warehouseAdminRepository
                .existsByAccountIdAndWarehouseId(warehouseAdminRequest.getAccountId(), warehouseAdminRequest.getWarehouseId());
        if (isExists) {
            throw new WarehouseAdminExistsException();
        }

        WarehouseAdmin warehouseAdmin = WarehouseAdmin
                .builder()
                .id(UUID.randomUUID())
                .account(account)
                .warehouse(warehouse)
                .build();

        warehouseAdminRepository.saveAndFlush(warehouseAdmin);

        return warehouseCustomRepository.getWarehouseAdmin(warehouseAdmin.getId());
    }

    public WarehouseAdminResponse patchWarehouseAdmin(UUID warehouseAdminId, WarehouseAdminRequest warehouseAdminRequest) {
        WarehouseAdmin warehouseAdmin = warehouseAdminRepository
                .findById(warehouseAdminId)
                .orElseThrow(WarehouseAdminNotFoundException::new);

        Account account = accountRepository
                .findById(warehouseAdminRequest.getAccountId())
                .orElseThrow(AccountNotFoundException::new);

        Warehouse warehouse = warehouseRepository
                .findById(warehouseAdminRequest.getWarehouseId())
                .orElseThrow(WarehouseNotFoundException::new);

        Boolean isAccountAnAdmin = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .anyMatch(permission -> permission.equals("SUPER_ADMIN") || permission.equals("WAREHOUSE_ADMIN"));
        if (!isAccountAnAdmin) {
            throw new AccountPermissionInvalidException();
        }

        warehouseAdmin.setAccount(account);
        warehouseAdmin.setWarehouse(warehouse);

        warehouseAdminRepository.saveAndFlush(warehouseAdmin);

        return warehouseCustomRepository.getWarehouseAdmin(warehouseAdmin.getId());
    }

    public void deleteWarehouseAdmin(UUID id) {
        WarehouseAdmin warehouseAdmin = warehouseAdminRepository
                .findById(id)
                .orElseThrow(WarehouseAdminNotFoundException::new);

        warehouseAdminRepository.delete(warehouseAdmin);
    }
}
