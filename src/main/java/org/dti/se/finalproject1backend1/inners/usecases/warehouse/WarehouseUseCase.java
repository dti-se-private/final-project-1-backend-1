package org.dti.se.finalproject1backend1.inners.usecases.warehouse;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouses.WarehouseRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouses.WarehouseResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.WarehouseCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountPermissionRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseAdminRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WarehouseUseCase {

    @Autowired
    WarehouseRepository warehouseRepository;

    @Autowired
    WarehouseCustomRepository warehouseCustomRepository;

    @Autowired
    AccountPermissionRepository accountPermissionRepository;
    @Autowired
    WarehouseAdminRepository warehouseAdminRepository;

    public List<WarehouseResponse> getWarehouses(
            Account account,
            Integer page,
            Integer size,
            String search
    ) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("SUPER_ADMIN")) {
            return warehouseCustomRepository.getWarehouses(page, size, search);
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            return warehouseCustomRepository.getAccountWarehouses(account, page, size, search);
        } else {
            throw new AccountPermissionInvalidException();
        }

    }

    public WarehouseResponse getWarehouse(Account account, UUID warehouseId) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("SUPER_ADMIN")) {
            return warehouseCustomRepository.getWarehouse(warehouseId);
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            return warehouseCustomRepository.getAccountWarehouse(account, warehouseId);
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

    public WarehouseResponse addWarehouse(WarehouseRequest request) {
        Warehouse warehouse = Warehouse.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .build();

        warehouseRepository.saveAndFlush(warehouse);

        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .description(warehouse.getDescription())
                .location(warehouse.getLocation())
                .build();
    }

    public WarehouseResponse patchWarehouse(Account account, UUID warehouseId, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository
                .findById(warehouseId)
                .orElseThrow(WarehouseNotFoundException::new);

        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("SUPER_ADMIN")) {
            // Do nothing.
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            Boolean isAccountAssociatedWithWarehouse = warehouseAdminRepository
                    .existsByAccountIdAndWarehouseId(account.getId(), warehouse.getId());
            if (!isAccountAssociatedWithWarehouse) {
                throw new AccountPermissionInvalidException();
            }
        } else {
            throw new AccountPermissionInvalidException();
        }

        warehouse.setName(request.getName());
        warehouse.setDescription(request.getDescription());
        warehouse.setLocation(request.getLocation());

        warehouseRepository.saveAndFlush(warehouse);

        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .description(warehouse.getDescription())
                .location(warehouse.getLocation())
                .build();

    }

    public void deleteWarehouse(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository
                .findById(warehouseId)
                .orElseThrow(WarehouseNotFoundException::new);

        warehouseRepository.deleteById(warehouseId);
    }
}