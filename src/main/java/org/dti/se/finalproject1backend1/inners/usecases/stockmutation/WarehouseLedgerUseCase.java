package org.dti.se.finalproject1backend1.inners.usecases.stockmutation;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.WarehouseLedgerResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.WarehouseLedgerCustomRepository;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseLedgerNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WarehouseLedgerUseCase {

    @Autowired
    private WarehouseLedgerCustomRepository ledgerCustomRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    public List<WarehouseLedgerResponse> getWarehouseLedgers(
            Account account,
            Integer page,
            Integer size,
            String search
    ) {
        // Direct permission check
        if (!account.getAccountPermissions().contains("WAREHOUSE_ADMIN") &&
                !account.getAccountPermissions().contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        List<UUID> warehouseIds = null;
        if (account.getAccountPermissions().contains("WAREHOUSE_ADMIN")) {
            warehouseIds = warehouseRepository.findWarehouseIdsByAccountId(account.getId());
        }

        // Proceed with the operation
        return ledgerCustomRepository.getWarehouseLedgers(warehouseIds, page, size, search);
    }

    public WarehouseLedgerResponse approveMutation(Account account, UUID id) {
        // Direct permission check (only SUPER_ADMIN can approve)
        if (!account.getAccountPermissions().contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        // Fetch the ledger entry to validate warehouse association
        WarehouseLedgerResponse ledger = getLedgerById(id);

        // Check if the ledger's origin or destination warehouse is associated with the warehouse admin
        if (account.getAccountPermissions().contains("WAREHOUSE_ADMIN")) {
            List<UUID> warehouseIds = warehouseRepository.findWarehouseIdsByAccountId(account.getId());
            if (!warehouseIds.contains(ledger.getOriginWarehouse().getId()) &&
                    !warehouseIds.contains(ledger.getDestinationWarehouse().getId())) {
                throw new AccountPermissionInvalidException();
            }
        }

        // Proceed with approval logic
        ledgerCustomRepository.approveMutation(id);
        return getLedgerById(id);
    }

    public WarehouseLedgerResponse rejectMutation(Account account, UUID id) {
        // Direct permission check (WAREHOUSE_ADMIN or SUPER_ADMIN can reject)
        if (!account.getAccountPermissions().contains("WAREHOUSE_ADMIN") &&
                !account.getAccountPermissions().contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        // Fetch the ledger entry to validate warehouse association
        WarehouseLedgerResponse ledger = getLedgerById(id);

        // Check if the ledger's origin or destination warehouse is associated with the warehouse admin
        if (account.getAccountPermissions().contains("WAREHOUSE_ADMIN")) {
            List<UUID> warehouseIds = warehouseRepository.findWarehouseIdsByAccountId(account.getId());
            if (!warehouseIds.contains(ledger.getOriginWarehouse().getId()) &&
                    !warehouseIds.contains(ledger.getDestinationWarehouse().getId())) {
                throw new AccountPermissionInvalidException();
            }
        }

        // Proceed with rejection logic
        ledgerCustomRepository.rejectMutation(id);
        return getLedgerById(id);
    }

    public WarehouseLedgerResponse addMutation(
            Account account,
            UUID productId,
            UUID originWarehouseId,
            UUID destinationWarehouseId,
            Double quantity
    ) {
        // Direct permission check (WAREHOUSE_ADMIN or SUPER_ADMIN can add mutations)
        if (!account.getAccountPermissions().contains("WAREHOUSE_ADMIN") &&
                !account.getAccountPermissions().contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        // Proceed with adding mutation
        return ledgerCustomRepository.addMutation(
                productId,
                originWarehouseId,
                destinationWarehouseId,
                quantity
        );
    }

    private WarehouseLedgerResponse getLedgerById(UUID id) {
        return ledgerCustomRepository.getWarehouseLedgers(null,0, 1, id.toString())
                .stream()
                .findFirst()
                .orElseThrow(WarehouseLedgerNotFoundException::new);
    }
}


