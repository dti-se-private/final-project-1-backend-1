package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseLedger;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.mutations.AddMutationRequest;
import org.dti.se.finalproject1backend1.inners.usecases.stockmutation.WarehouseLedgerUseCase;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/warehouse-ledgers")
@RequiredArgsConstructor
public class WarehouseLedgerRest {
    private final WarehouseLedgerUseCase warehouseLedgerService;

    @GetMapping
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<Page<WarehouseLedger>> getWarehouseLedgers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filters,
            @RequestParam(required = false) String search) {
        Page<WarehouseLedger> ledgers = warehouseLedgerService.getWarehouseLedgers(page, size, filters, search);
        return ResponseEntity.ok(ledgers);
    }

    @PostMapping("/mutations/add")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<WarehouseLedger> addMutation(@RequestBody AddMutationRequest request) {
        WarehouseLedger ledger = warehouseLedgerService.addLedgerMutation(
                request.getProductId(),
                request.getOriginWarehouseId(),
                request.getDestinationWarehouseId(),
                request.getQuantity()
        );
        return ResponseEntity.ok(ledger);
    }

    @PostMapping("/mutations/approve")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<WarehouseLedger> approveMutation(@RequestParam UUID id) {
        return ResponseEntity.ok(warehouseLedgerService.approveLedgerMutation(id));
    }

    @PostMapping("/mutations/reject")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<WarehouseLedger> rejectMutation(@RequestParam UUID id) {
        return ResponseEntity.ok(warehouseLedgerService.rejectLedgerMutation(id));
    }
}
