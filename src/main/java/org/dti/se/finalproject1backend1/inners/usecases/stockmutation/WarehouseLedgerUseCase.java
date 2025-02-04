package org.dti.se.finalproject1backend1.inners.usecases.stockmutation;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseLedger;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseProduct;
import org.dti.se.finalproject1backend1.outers.repositories.ones.ProductRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseLedgerRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseProductRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseLedgerUseCase {

    private final WarehouseLedgerRepository warehouseLedgerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final WarehouseProductRepository warehouseProductRepository;

    public Page<WarehouseLedger> getWarehouseLedgers(int page, int size, String filters, String search) {
        // Apply filtering and searching logic here (if needed)
        return warehouseLedgerRepository.findAll(PageRequest.of(page, size));
    }

    public Optional<WarehouseLedger> getLedgerById(UUID id) {
        return warehouseLedgerRepository.findById(id);
    }

    public WarehouseLedger addLedgerMutation(UUID productId, UUID originWarehouseId, UUID destinationWarehouseId, Double quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Warehouse originWarehouse = warehouseRepository.findById(originWarehouseId)
                .orElseThrow(() -> new RuntimeException("Origin warehouse not found"));
        Warehouse destinationWarehouse = warehouseRepository.findById(destinationWarehouseId)
                .orElseThrow(() -> new RuntimeException("Destination warehouse not found"));

        // Fetch warehouse product for origin and destination
        WarehouseProduct originProduct = warehouseProductRepository.findByProductIdAndWarehouseId(originWarehouse.getId(), product.getId())
                .orElseThrow(() -> new RuntimeException("Product not found in origin warehouse"));

        WarehouseProduct destinationProduct = warehouseProductRepository.findByProductIdAndWarehouseId(destinationWarehouse.getId(), product.getId())
                .orElseGet(() -> {
                    // If product doesn't exist in destination warehouse, create a new record
                    WarehouseProduct newProduct = new WarehouseProduct();
                    newProduct.setId(UUID.randomUUID());
                    newProduct.setWarehouse(destinationWarehouse);
                    newProduct.setProduct(product);
                    newProduct.setQuantity(0.0);
                    return warehouseProductRepository.save(newProduct);
                });

        // Ensure there's enough stock in origin
        if (originProduct.getQuantity() < 0) {
            throw new RuntimeException("Insufficient stock in origin warehouse");
        }
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        // Create ledger entry
        WarehouseLedger ledger = new WarehouseLedger();
        ledger.setId(UUID.randomUUID());
        ledger.setProduct(product);
        ledger.setOriginWarehouse(originWarehouse);
        ledger.setDestinationWarehouse(destinationWarehouse);
        ledger.setOriginPreQuantity(originProduct.getQuantity());
        ledger.setOriginPostQuantity(originProduct.getQuantity() - quantity);
        ledger.setTime(now);
        ledger.setStatus("WAITING_APPROVAL");

        return warehouseLedgerRepository.save(ledger);
    }

    public WarehouseLedger approveLedgerMutation(UUID ledgerId) {
        WarehouseLedger ledger = warehouseLedgerRepository.findById(ledgerId)
                .orElseThrow(() -> new RuntimeException("Ledger not found"));

        if (!ledger.getStatus().equals("WAITING_APPROVAL")) {
            throw new RuntimeException("Mutation is already processed");
        }

        // Update warehouse product stock
        WarehouseProduct originProduct = warehouseProductRepository.findByProductIdAndWarehouseId(ledger.getOriginWarehouse().getId(), ledger.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Origin warehouse product not found"));
        WarehouseProduct destinationProduct = warehouseProductRepository.findByProductIdAndWarehouseId(ledger.getDestinationWarehouse().getId(), ledger.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Destination warehouse product not found"));

        originProduct.setQuantity(originProduct.getQuantity() - ledger.getOriginPostQuantity());
        destinationProduct.setQuantity(destinationProduct.getQuantity() + ledger.getOriginPostQuantity());

        warehouseProductRepository.save(originProduct);
        warehouseProductRepository.save(destinationProduct);

        // Update ledger status
        ledger.setStatus("APPROVED");
        return warehouseLedgerRepository.save(ledger);
    }

    public WarehouseLedger rejectLedgerMutation(UUID ledgerId) {
        WarehouseLedger ledger = warehouseLedgerRepository.findById(ledgerId)
                .orElseThrow(() -> new RuntimeException("Ledger not found"));

        if (!ledger.getStatus().equals("WAITING_APPROVAL")) {
            throw new RuntimeException("Mutation is already processed");
        }

        // Simply mark the ledger as rejected
        ledger.setStatus("REJECTED");
        return warehouseLedgerRepository.save(ledger);
    }
}


