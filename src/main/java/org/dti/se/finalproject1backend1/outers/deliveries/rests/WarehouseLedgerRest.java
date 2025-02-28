package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.AddMutationRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.ApprovalMutationRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.WarehouseLedgerResponse;
import org.dti.se.finalproject1backend1.inners.usecases.stockmutation.WarehouseLedgerUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/warehouse-ledgers")
@RequiredArgsConstructor
public class WarehouseLedgerRest {

    private final WarehouseLedgerUseCase warehouseLedgerService;

    @GetMapping("/mutations")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<List<WarehouseLedgerResponse>>> getMutationRequests(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<WarehouseLedgerResponse> warehouseLedgers = warehouseLedgerService.getMutationRequests(
                    account, page, size, search
            );
            return ResponseBody
                    .<List<WarehouseLedgerResponse>>builder()
                    .message("Stock mutation requests found.")
                    .data(warehouseLedgers)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<List<WarehouseLedgerResponse>>builder()
                    .message("Account permission invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return ResponseBody
                    .<List<WarehouseLedgerResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/mutations/{warehouseLedgerId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseLedgerResponse>> getMutationRequest(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID warehouseLedgerId
    ) {
        try {
            WarehouseLedgerResponse warehouseLedger = warehouseLedgerService.getMutationRequest(
                    account, warehouseLedgerId
            );
            return ResponseBody
                    .<WarehouseLedgerResponse>builder()
                    .message("Stock mutation request found.")
                    .data(warehouseLedger)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseLedgerResponse>builder()
                    .message("Account permission invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseLedgerResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/mutations/add")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<Void>> addMutationRequest(
            @RequestBody AddMutationRequest request
    ) {
        try {
            warehouseLedgerService.addMutationRequest(request);
            return ResponseBody
                    .<Void>builder()
                    .message("Stock mutation request added.")
                    .data(null)
                    .build()
                    .toEntity(HttpStatus.CREATED);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Account permission invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (WarehouseLedgerQuantityInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Warehouse ledger quantity invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (WarehouseProductNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Product not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/mutations/approve")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<Void>> approveMutationRequest(
            @AuthenticationPrincipal Account account,
            @RequestBody ApprovalMutationRequest request
    ) {
        try {
            warehouseLedgerService.approveMutationRequest(account, request.getWarehouseLedgerId());
            return ResponseBody
                    .<Void>builder()
                    .message("Stock mutation request approved.")
                    .data(null)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Account permission invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (WarehouseLedgerNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Ledger not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (WarehouseLedgerApprovalInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Warehouse ledger approval invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (WarehouseLedgerWarehouseInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Warehouse ledger warehouse invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (WarehouseLedgerQuantityInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Warehouse ledger quantity invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (WarehouseProductNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Product not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/mutations/reject")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<Void>> rejectMutationRequest(
            @AuthenticationPrincipal Account account,
            @RequestBody ApprovalMutationRequest request
    ) {
        try {
            warehouseLedgerService.rejectMutationRequest(account, request.getWarehouseLedgerId());
            return ResponseBody
                    .<Void>builder()
                    .message("Stock mutation request rejected.")
                    .data(null)
                    .build()
                    .toEntity(HttpStatus.OK);

        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Account permission invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (WarehouseLedgerNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Ledger not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (WarehouseLedgerApprovalInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Warehouse ledger approval invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (WarehouseLedgerWarehouseInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Warehouse ledger warehouse invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (WarehouseLedgerQuantityInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Warehouse ledger quantity invalid.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}