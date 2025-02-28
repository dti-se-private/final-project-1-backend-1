package org.dti.se.finalproject1backend1.inners.usecases.stockmutation;

import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.AddMutationRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.WarehouseLedgerResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.*;
import org.dti.se.finalproject1backend1.outers.repositories.customs.WarehouseLedgerCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.StockLedgerRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseLedgerRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class WarehouseLedgerUseCase {

    @Autowired
    WarehouseLedgerRepository warehouseLedgerRepository;
    @Autowired
    WarehouseProductRepository warehouseProductRepository;
    @Autowired
    StockLedgerRepository stockLedgerRepository;
    @Autowired
    WarehouseLedgerCustomRepository warehouseLedgerCustomRepository;

    public List<WarehouseLedgerResponse> getMutationRequests(
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
            return warehouseLedgerCustomRepository.getOriginWarehouseLedgers(page, size, search);
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            return warehouseLedgerCustomRepository.getOriginWarehouseLedgers(account, page, size, search);
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

    public WarehouseLedgerResponse getMutationRequest(Account account, UUID warehouseLedgerId) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("SUPER_ADMIN")) {
            return warehouseLedgerCustomRepository.getOriginWarehouseLedger(warehouseLedgerId);
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            return warehouseLedgerCustomRepository.getOriginWarehouseLedger(account, warehouseLedgerId);
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

    public void approveMutationRequest(Account account, UUID warehouseLedgerId) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        WarehouseLedger foundWarehouseLedger = warehouseLedgerRepository
                .findById(warehouseLedgerId)
                .orElseThrow(WarehouseLedgerNotFoundException::new);

        if (!foundWarehouseLedger.getStatus().equals("WAITING_FOR_APPROVAL")) {
            throw new WarehouseLedgerApprovalInvalidException();
        }

        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("SUPER_ADMIN")) {
            // Do nothing.
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            Boolean isAccountRelatedToOriginWarehouseLedger = warehouseLedgerCustomRepository
                    .isAccountRelatedToOriginWarehouseLedger(account, warehouseLedgerId);
            if (!isAccountRelatedToOriginWarehouseLedger) {
                throw new AccountPermissionInvalidException();
            }
        } else {
            throw new AccountPermissionInvalidException();
        }

        WarehouseProduct originWarehouseProduct = foundWarehouseLedger
                .getOriginWarehouseProduct();

        WarehouseProduct destinationWarehouseProduct = foundWarehouseLedger
                .getDestinationWarehouseProduct();

        if (originWarehouseProduct.getWarehouse().getId().equals(destinationWarehouseProduct.getWarehouse().getId())) {
            throw new WarehouseLedgerWarehouseInvalidException();
        }

        Double originQuantity = foundWarehouseLedger.getOriginPreQuantity() - foundWarehouseLedger.getOriginPostQuantity();
        Double destinationQuantity = foundWarehouseLedger.getDestinationPostQuantity() - foundWarehouseLedger.getDestinationPreQuantity();
        if (originQuantity < 0 || destinationQuantity < 0) {
            throw new WarehouseLedgerQuantityInvalidException();
        }

        Double originWarehousePostQuantity = originWarehouseProduct.getQuantity() - originQuantity;
        Double destinationWarehousePostQuantity = destinationWarehouseProduct.getQuantity() + destinationQuantity;
        if (originWarehousePostQuantity < 0 || destinationWarehousePostQuantity < 0) {
            throw new WarehouseProductInsufficientException();
        }

        StockLedger originStockLedger = StockLedger
                .builder()
                .id(UUID.randomUUID())
                .warehouseProduct(originWarehouseProduct)
                .preQuantity(originWarehouseProduct.getQuantity())
                .postQuantity(originWarehousePostQuantity)
                .time(now)
                .build();
        stockLedgerRepository.saveAndFlush(originStockLedger);

        StockLedger destinationStockLedger = StockLedger
                .builder()
                .id(UUID.randomUUID())
                .warehouseProduct(destinationWarehouseProduct)
                .preQuantity(destinationWarehouseProduct.getQuantity())
                .postQuantity(destinationWarehousePostQuantity)
                .time(now)
                .build();
        stockLedgerRepository.saveAndFlush(destinationStockLedger);

        foundWarehouseLedger
                .setOriginWarehouseProduct(originWarehouseProduct)
                .setDestinationWarehouseProduct(destinationWarehouseProduct)
                .setOriginPreQuantity(originWarehouseProduct.getQuantity())
                .setOriginPostQuantity(originWarehousePostQuantity)
                .setDestinationPreQuantity(destinationWarehouseProduct.getQuantity())
                .setDestinationPostQuantity(destinationWarehousePostQuantity)
                .setStatus("APPROVED")
                .setTime(now);

        originWarehouseProduct.setQuantity(originWarehousePostQuantity);
        destinationWarehouseProduct.setQuantity(destinationWarehousePostQuantity);
        warehouseProductRepository.saveAndFlush(originWarehouseProduct);
        warehouseProductRepository.saveAndFlush(destinationWarehouseProduct);
        warehouseLedgerRepository.saveAndFlush(foundWarehouseLedger);

    }

    public void rejectMutationRequest(Account account, UUID warehouseLedgerId) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        WarehouseLedger foundWarehouseLedger = warehouseLedgerRepository
                .findById(warehouseLedgerId)
                .orElseThrow(WarehouseLedgerNotFoundException::new);

        if (!foundWarehouseLedger.getStatus().equals("WAITING_FOR_APPROVAL")) {
            throw new WarehouseLedgerApprovalInvalidException();
        }

        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("SUPER_ADMIN")) {
            // Do nothing.
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            Boolean isAccountRelatedToOriginWarehouseLedger = warehouseLedgerCustomRepository
                    .isAccountRelatedToOriginWarehouseLedger(account, warehouseLedgerId);
            if (!isAccountRelatedToOriginWarehouseLedger) {
                throw new AccountPermissionInvalidException();
            }
        } else {
            throw new AccountPermissionInvalidException();
        }

        WarehouseProduct originWarehouseProduct = foundWarehouseLedger
                .getOriginWarehouseProduct();

        WarehouseProduct destinationWarehouseProduct = foundWarehouseLedger
                .getDestinationWarehouseProduct();

        if (originWarehouseProduct.getWarehouse().getId().equals(destinationWarehouseProduct.getWarehouse().getId())) {
            throw new WarehouseLedgerWarehouseInvalidException();
        }

        Double originQuantity = foundWarehouseLedger.getOriginPreQuantity() - foundWarehouseLedger.getOriginPostQuantity();
        Double destinationQuantity = foundWarehouseLedger.getDestinationPostQuantity() - foundWarehouseLedger.getDestinationPreQuantity();
        if (originQuantity < 0 || destinationQuantity < 0) {
            throw new WarehouseLedgerQuantityInvalidException();
        }

        foundWarehouseLedger
                .setStatus("REJECTED")
                .setTime(now);

        warehouseLedgerRepository.saveAndFlush(foundWarehouseLedger);
    }

    public void addMutationRequest(AddMutationRequest request) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        WarehouseProduct originWarehouseProduct = warehouseProductRepository
                .findByProductIdAndWarehouseId(request.getProductId(), request.getOriginWarehouseId())
                .orElseThrow(WarehouseProductNotFoundException::new);

        WarehouseProduct destinationWarehouseProduct = warehouseProductRepository
                .findByProductIdAndWarehouseId(request.getProductId(), request.getDestinationWarehouseId())
                .orElseThrow(WarehouseProductNotFoundException::new);

        if (originWarehouseProduct.getWarehouse().getId().equals(destinationWarehouseProduct.getWarehouse().getId())) {
            throw new WarehouseLedgerQuantityInvalidException();
        }

        Double originWarehousePostQuantity = originWarehouseProduct.getQuantity() - request.getQuantity();
        Double destinationWarehousePostQuantity = destinationWarehouseProduct.getQuantity() + request.getQuantity();

        WarehouseLedger newWarehouseLedger = WarehouseLedger
                .builder()
                .id(UUID.randomUUID())
                .originWarehouseProduct(originWarehouseProduct)
                .destinationWarehouseProduct(destinationWarehouseProduct)
                .originPreQuantity(originWarehouseProduct.getQuantity())
                .originPostQuantity(originWarehousePostQuantity)
                .destinationPreQuantity(destinationWarehouseProduct.getQuantity())
                .destinationPostQuantity(destinationWarehousePostQuantity)
                .time(now)
                .status("WAITING_FOR_APPROVAL")
                .build();

        warehouseLedgerRepository.saveAndFlush(newWarehouseLedger);
    }

}


