package org.dti.se.finalproject1backend1.inners.usecases.warehouse;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.products.ProductNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseProductExistsException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseProductNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.WarehouseProductCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.*;
import org.dti.se.finalproject1backend1.outers.utilities.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseProductUseCase {
    @Autowired
    WarehouseProductRepository warehouseProductRepository;
    @Autowired
    WarehouseProductCustomRepository warehouseProductCustomRepository;
    @Autowired
    WarehouseRepository warehouseRepository;
    @Autowired
    StockLedgerRepository stockLedgerRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    WarehouseAdminRepository warehouseAdminRepository;

    public List<WarehouseProductResponse> getWarehouseProducts(
            Account account,
            Integer page,
            Integer size,
            String search
    ) {
        if (PermissionUtil.isSuperAdmin(account)) {
            return warehouseProductCustomRepository
                    .getWarehouseProducts(page, size, search);
        } else if (PermissionUtil.isWarehouseAdmin(account)) {
            return warehouseProductCustomRepository
                    .getAccountWarehouseProducts(account, page, size, search);
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

    public WarehouseProductResponse getWarehouseProduct(Account account, UUID warehouseProductId) {
        if (PermissionUtil.isSuperAdmin(account)) {
            return warehouseProductCustomRepository
                    .getWarehouseProduct(warehouseProductId);
        } else if (PermissionUtil.isWarehouseAdmin(account)) {
            return warehouseProductCustomRepository
                    .getAccountWarehouseProduct(account, warehouseProductId);
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

    public WarehouseProductResponse addWarehouseProduct(Account account, WarehouseProductRequest request) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        Warehouse warehouse = warehouseRepository
                .findById(request.getWarehouseId())
                .orElseThrow(WarehouseNotFoundException::new);

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(ProductNotFoundException::new);

        Boolean isExists = warehouseProductRepository
                .existsByProductIdAndWarehouseId(request.getProductId(), request.getWarehouseId());

        if (isExists) {
            throw new WarehouseProductExistsException();
        }

        if (PermissionUtil.isSuperAdmin(account)) {
            // Do nothing.
        } else if (PermissionUtil.isWarehouseAdmin(account)) {
            Boolean isAccountAssociatedWithWarehouse = warehouseAdminRepository
                    .existsByAccountIdAndWarehouseId(account.getId(), request.getWarehouseId());
            if (!isAccountAssociatedWithWarehouse) {
                throw new AccountPermissionInvalidException();
            }
        } else {
            throw new AccountPermissionInvalidException();
        }

        WarehouseProduct warehouseProduct = WarehouseProduct
                .builder()
                .id(UUID.randomUUID())
                .warehouse(warehouse)
                .product(product)
                .quantity(request.getQuantity())
                .build();
        warehouseProductRepository.saveAndFlush(warehouseProduct);

        StockLedger stockLedger = StockLedger
                .builder()
                .id(UUID.randomUUID())
                .warehouseProduct(warehouseProduct)
                .preQuantity(0.0)
                .postQuantity(warehouseProduct.getQuantity())
                .time(now)
                .build();
        stockLedgerRepository.saveAndFlush(stockLedger);


        return warehouseProductCustomRepository.getWarehouseProduct(warehouseProduct.getId());
    }

    public WarehouseProductResponse patchWarehouseProduct(Account account, UUID warehouseProductId, WarehouseProductRequest request) {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        Warehouse warehouse = warehouseRepository
                .findById(request.getWarehouseId())
                .orElseThrow(WarehouseNotFoundException::new);

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(ProductNotFoundException::new);

        WarehouseProduct warehouseProduct = warehouseProductRepository
                .findById(warehouseProductId)
                .orElseThrow(WarehouseProductNotFoundException::new);

        if (PermissionUtil.isSuperAdmin(account)) {
            // Do nothing.
        } else if (PermissionUtil.isWarehouseAdmin(account)) {
            Boolean isAccountAssociatedWithWarehouse = warehouseAdminRepository
                    .existsByAccountIdAndWarehouseId(account.getId(), request.getWarehouseId());
            if (!isAccountAssociatedWithWarehouse) {
                throw new AccountPermissionInvalidException();
            }
        } else {
            throw new AccountPermissionInvalidException();
        }

        StockLedger stockLedger = StockLedger
                .builder()
                .id(UUID.randomUUID())
                .warehouseProduct(warehouseProduct)
                .preQuantity(warehouseProduct.getQuantity())
                .postQuantity(request.getQuantity())
                .time(now)
                .build();
        stockLedgerRepository.saveAndFlush(stockLedger);

        warehouseProduct.setWarehouse(warehouse);
        warehouseProduct.setProduct(product);
        warehouseProduct.setQuantity(request.getQuantity());
        warehouseProductRepository.saveAndFlush(warehouseProduct);

        return warehouseProductCustomRepository.getWarehouseProduct(warehouseProduct.getId());
    }

    public void deleteWarehouseProduct(Account account, UUID warehouseProductId) {
        WarehouseProduct warehouseProduct = warehouseProductRepository
                .findById(warehouseProductId)
                .orElseThrow(WarehouseProductNotFoundException::new);

        if (PermissionUtil.isSuperAdmin(account)) {
            // Do nothing.
        } else if (PermissionUtil.isWarehouseAdmin(account)) {
            Boolean isAccountAssociatedWithWarehouse = warehouseAdminRepository
                    .existsByAccountIdAndWarehouseId(account.getId(), warehouseProduct.getWarehouse().getId());
            if (!isAccountAssociatedWithWarehouse) {
                throw new AccountPermissionInvalidException();
            }
        } else {
            throw new AccountPermissionInvalidException();
        }

        warehouseProductRepository.deleteById(warehouseProductId);
    }
}
