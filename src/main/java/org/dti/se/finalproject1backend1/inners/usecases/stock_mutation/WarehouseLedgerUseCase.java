package org.dti.se.finalproject1backend1.inners.usecases.stock_mutation;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.outers.repositories.ones.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
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
        return warehouseLedgerRepository.findAll(PageRequest.of(page, size));
    }

    public Optional<WarehouseLedger> getLedgerById(UUID id) {
        return warehouseLedgerRepository.findById(id);
    }

    @Transactional
    public WarehouseLedger addManualMutation(UUID productId, UUID originWarehouseId,
                                             UUID destinationWarehouseId, BigDecimal quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Warehouse origin = warehouseRepository.findById(originWarehouseId)
                .orElseThrow(() -> new RuntimeException("Origin warehouse not found"));

        Warehouse destination = warehouseRepository.findById(destinationWarehouseId)
                .orElseThrow(() -> new RuntimeException("Destination warehouse not found"));

        WarehouseProduct senderProduct = warehouseProductRepository.findByProductAndWarehouse(origin.getId(), product.getId())
                .orElseThrow(() -> new RuntimeException("Product not found in origin warehouse"));

        if (senderProduct.getQuantity().compareTo(quantity) < 0) {
            throw new RuntimeException("Insufficient stock in origin warehouse");
        }

        WarehouseLedger ledger = new WarehouseLedger();
        ledger.setId(UUID.randomUUID());
        ledger.setProduct(product);
        ledger.setOriginWarehouse(origin);
        ledger.setDestinationWarehouse(destination);
        ledger.setPreQuantity(senderProduct.getQuantity());
        ledger.setPostQuantity(senderProduct.getQuantity().subtract(quantity));
        ledger.setTime(OffsetDateTime.now());
        ledger.setStatus("WAITING_APPROVAL");

        return warehouseLedgerRepository.save(ledger);
    }

    @Transactional
    public WarehouseLedger rejectStockMutation(UUID ledgerId) {
        WarehouseLedger ledger = warehouseLedgerRepository.findById(ledgerId)
                .orElseThrow(() -> new RuntimeException("Ledger not found"));

        if (!ledger.getStatus().equals("WAITING_APPROVAL")) {
            throw new RuntimeException("Only pending mutations can be rejected");
        }

        ledger.setStatus("REJECTED");
        return warehouseLedgerRepository.save(ledger);
    }

    @Transactional
    public WarehouseLedger requestStockMutation(UUID productId, UUID requesterWarehouseId, BigDecimal quantity, boolean isManual) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Warehouse requesterWarehouse = warehouseRepository.findById(requesterWarehouseId)
                .orElseThrow(() -> new RuntimeException("Requester warehouse not found"));

        Warehouse nearestWarehouse = findNearestWarehouseWithStock(productId, requesterWarehouseId, quantity);
        if (nearestWarehouse == null) {
            throw new RuntimeException("No warehouse with sufficient stock found");
        }


        WarehouseProduct senderProduct = warehouseProductRepository.findByProductAndWarehouse(nearestWarehouse.getId(), product.getId())
                .orElseThrow(() -> new RuntimeException("Product not found in requested warehouse"));
        WarehouseProduct receiverProduct = warehouseProductRepository.findByProductAndWarehouse(requesterWarehouse.getId(), product.getId())
                .orElseGet(() -> {
                    WarehouseProduct newProduct = new WarehouseProduct();
                    newProduct.setId(UUID.randomUUID());
                    newProduct.setWarehouse(requesterWarehouse);
                    newProduct.setProduct(product);
                    newProduct.setQuantity(BigDecimal.ZERO);
                    return warehouseProductRepository.save(newProduct);
                });

        if (senderProduct.getQuantity().compareTo(quantity) < 0) {
            throw new RuntimeException("Insufficient stock in requested warehouse");
        }

        WarehouseLedger ledger = new WarehouseLedger();
        ledger.setId(UUID.randomUUID());
        ledger.setProduct(product);
        ledger.setOriginWarehouse(nearestWarehouse);
        ledger.setDestinationWarehouse(requesterWarehouse);
        ledger.setPreQuantity(senderProduct.getQuantity());
        ledger.setPostQuantity(senderProduct.getQuantity().subtract(quantity));
        ledger.setTime(OffsetDateTime.now());
        ledger.setStatus(isManual ? "WAITING_APPROVAL" : "APPROVED");

        WarehouseLedger savedLedger = warehouseLedgerRepository.save(ledger);

        // Automatically complete if not manual
        if (!isManual) {
            completeStockMutation(savedLedger);

            // Validate automatic request comes from nearest warehouse
            if (!requesterWarehouseId.equals(nearestWarehouse.getId())) {
                throw new RuntimeException("Automatic requests must use nearest warehouse");
            }
        }

        return savedLedger;
    }

    @Transactional
    public WarehouseLedger approveStockMutation(UUID ledgerId, boolean isRequesterApproval) {
        WarehouseLedger ledger = warehouseLedgerRepository.findById(ledgerId)
                .orElseThrow(() -> new RuntimeException("Ledger not found"));

        String currentStatus = ledger.getStatus();

        if (currentStatus.equals("COMPLETED")) {
            throw new RuntimeException("Mutation already completed");
        }

        if (!currentStatus.equals("WAITING_APPROVAL") &&
                !currentStatus.equals("REQUESTER_APPROVED") &&
                !currentStatus.equals("REQUESTED_APPROVED")) {
            throw new RuntimeException("Invalid status for approval: " + currentStatus);
        }

        // State machine transitions
        if (currentStatus.equals("WAITING_APPROVAL")) {
            if (isRequesterApproval) {
                ledger.setStatus("REQUESTER_APPROVED");
            } else {
                ledger.setStatus("REQUESTED_APPROVED");
            }
        } else if (currentStatus.equals("REQUESTER_APPROVED") && !isRequesterApproval) {
            completeStockMutation(ledger);
        } else if (currentStatus.equals("REQUESTED_APPROVED") && isRequesterApproval) {
            completeStockMutation(ledger);
        } else {
            throw new RuntimeException("Invalid approval sequence");
        }

        return warehouseLedgerRepository.save(ledger);
    }

    // Add overloaded method for simple approval
    public WarehouseLedger approveStockMutation(UUID ledgerId) {
        return approveStockMutation(ledgerId, false); // Default to requested approval
    }

    private void completeStockMutation(WarehouseLedger ledger) {
        // Update stock quantities
        WarehouseProduct senderProduct = warehouseProductRepository.findByProductAndWarehouse(
                ledger.getOriginWarehouse().getId(),
                ledger.getProduct().getId()
        ).orElseThrow(() -> new RuntimeException("Sender warehouse product not found"));

        WarehouseProduct receiverProduct = warehouseProductRepository.findByProductAndWarehouse(
                ledger.getDestinationWarehouse().getId(),
                ledger.getProduct().getId()
        ).orElseGet(() -> createNewWarehouseProduct(ledger));

        BigDecimal transferredAmount = ledger.getPreQuantity().subtract(ledger.getPostQuantity());

        // Update sender
        senderProduct.setQuantity(ledger.getPostQuantity());

        // Update receiver
        BigDecimal receiverNewQuantity = receiverProduct.getQuantity().add(transferredAmount);
        receiverProduct.setQuantity(receiverNewQuantity);

        warehouseProductRepository.saveAll(List.of(senderProduct, receiverProduct));

        // Create receiver ledger entry
        WarehouseLedger receiverLedger = new WarehouseLedger();
        receiverLedger.setId(UUID.randomUUID());
        receiverLedger.setProduct(ledger.getProduct());
        receiverLedger.setOriginWarehouse(ledger.getOriginWarehouse());
        receiverLedger.setDestinationWarehouse(ledger.getDestinationWarehouse());
        receiverLedger.setPreQuantity(receiverProduct.getQuantity().subtract(transferredAmount));
        receiverLedger.setPostQuantity(receiverNewQuantity);
        receiverLedger.setTime(OffsetDateTime.now());
        receiverLedger.setStatus("COMPLETED");

        warehouseLedgerRepository.save(receiverLedger);

        // Update original ledger status
        ledger.setStatus("COMPLETED");
    }

    private Warehouse findNearestWarehouseWithStock(UUID productId, UUID requesterWarehouseId, BigDecimal requiredQuantity) {
        return warehouseRepository.findNearestWarehouseWithStock(productId, requesterWarehouseId, requiredQuantity)
                .orElse(null);
    }

    private WarehouseProduct createNewWarehouseProduct(WarehouseLedger ledger) {
        WarehouseProduct wp = new WarehouseProduct();
        wp.setId(UUID.randomUUID());
        wp.setWarehouse(ledger.getDestinationWarehouse());
        wp.setProduct(ledger.getProduct());
        wp.setQuantity(BigDecimal.ZERO);
        return warehouseProductRepository.save(wp);
    }
}

