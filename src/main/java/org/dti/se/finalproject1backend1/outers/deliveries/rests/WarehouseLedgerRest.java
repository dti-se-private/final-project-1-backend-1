package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseLedger;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseProduct;
import org.dti.se.finalproject1backend1.inners.usecases.stock_mutation.WarehouseLedgerUseCase;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/warehouse-ledgers")
@RequiredArgsConstructor
public class WarehouseLedgerRest {
    private final WarehouseLedgerUseCase warehouseLedgerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<Page<WarehouseLedgerResponse>> getWarehouseLedgers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filters,
            @RequestParam(required = false) String search) {
        Page<WarehouseLedger> ledgers = warehouseLedgerService.getWarehouseLedgers(page, size, filters, search);
        Page<WarehouseLedgerResponse> response = ledgers.map(this::mapToResponse);
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/mutations/request")
//    public ResponseEntity<?> requestStockMutation(@RequestBody StockMutationRequest request) {
//        try {
//            WarehouseLedger ledger = warehouseLedgerService.requestStockMutation(
//                    request.productId(),
//                    request.requesterWarehouseId(),
//                    request.quantity(),
//                    request.isManual()
//            );
//            return ResponseEntity.ok(ledger);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

    @PostMapping("/mutations/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<WarehouseLedgerResponse> approveMutation(@RequestBody ApprovalRequest request) {
        WarehouseLedger ledger = warehouseLedgerService.approveStockMutation(
                request.id(),
                true // Assuming approval by super admin
        );
        return ResponseEntity.ok(mapToResponse(ledger));
    }

//    @PutMapping("/mutations/approve/{ledgerId}")
//    public ResponseEntity<?> approveStockMutation(
//            @PathVariable UUID ledgerId,
//            @RequestBody ApprovalRequest approvalRequest
//    ) {
//        try {
//            WarehouseLedger ledger = warehouseLedgerService.approveStockMutation(
//                    ledgerId,
//                    approvalRequest.isRequesterApproval()
//            );
//            return ResponseEntity.ok(ledger);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

    @PostMapping("/mutations/reject")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<WarehouseLedgerResponse> rejectMutation(@RequestBody RejectionRequest request) {
        WarehouseLedger ledger = warehouseLedgerService.rejectStockMutation(request.id());
        return ResponseEntity.ok(mapToResponse(ledger));
    }

    @PostMapping("/mutations/add")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<WarehouseLedgerResponse> addMutation(@RequestBody AddMutationRequest request) {
        WarehouseLedger ledger = warehouseLedgerService.addManualMutation(
                request.productId(),
                request.originWarehouseId(),
                request.destinationWarehouseId(),
                request.quantity()
        );
        return ResponseEntity.ok(mapToResponse(ledger));
    }
    // Remove reject mutation endpoints since not implemented in service
    // Or implement rejection logic in service first

    // DTO Records
    public record StockMutationRequest(
            UUID productId,
            UUID requesterWarehouseId,
            BigDecimal quantity,
            boolean isManual
    ) {}

    // Corrected DTO records
    public record ApprovalRequest(UUID id) {}  // For POST /mutations/approve

//    public record DetailedApprovalRequest(boolean isRequesterApproval) {}  // For PUT /mutations/approve/{id}

    // DTO Records
    public record WarehouseLedgerResponse(
            UUID id,
            WarehouseProduct warehouseProduct,
            BigDecimal preQuantity,
            BigDecimal postQuantity,
            OffsetDateTime time,
            boolean isApproved
    ) {}

    public record RejectionRequest(UUID id) {}
    public record AddMutationRequest(
            UUID productId,
            UUID originWarehouseId,
            UUID destinationWarehouseId,
            BigDecimal quantity
    ) {}

    // Mapping helper
    private WarehouseLedgerResponse mapToResponse(WarehouseLedger ledger) {
        return new WarehouseLedgerResponse(
                ledger.getId(),
                ledger.getWarehouseProduct(),
                ledger.getPreQuantity(),
                ledger.getPostQuantity(),
                ledger.getTime(),
                ledger.getStatus().equals("COMPLETED")
        );
    }
}