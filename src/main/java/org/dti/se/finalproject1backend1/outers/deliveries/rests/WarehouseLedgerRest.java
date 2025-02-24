package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.AddMutationRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.ApproveRejectRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation.WarehouseLedgerResponse;
import org.dti.se.finalproject1backend1.inners.usecases.stockmutation.WarehouseLedgerUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseLedgerNotFoundException;
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

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<List<WarehouseLedgerResponse>>> getWarehouseLedgers(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        try {
            List<WarehouseLedgerResponse> ledgers = warehouseLedgerService.getWarehouseLedgers(
                    account, page, size, search
            );
            return ResponseBody
                    .<List<WarehouseLedgerResponse>>builder()
                    .message("Ledgers retrieved successfully.")
                    .data(ledgers)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<List<WarehouseLedgerResponse>>builder()
                    .message("Permission denied.")
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

    @PostMapping("/mutations/add")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseLedgerResponse>> addMutation(
            @AuthenticationPrincipal Account account,
            @RequestBody AddMutationRequest request
    ) {
        try {
            WarehouseLedgerResponse ledger = warehouseLedgerService.addMutation(
                    account,
                    request.getProductId(),
                    request.getOriginWarehouseId(),
                    request.getDestinationWarehouseId(),
                    request.getQuantity()
            );
            return ResponseBody
                    .<WarehouseLedgerResponse>builder()
                    .message("Mutation added successfully.")
                    .data(ledger)
                    .build()
                    .toEntity(HttpStatus.CREATED);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseLedgerResponse>builder()
                    .message("Permission denied.")
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

    @PostMapping("/mutations/approve")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseLedgerResponse>> approveMutation(
            @AuthenticationPrincipal Account account,
            @RequestBody ApproveRejectRequest request
    ) {
        try {
            WarehouseLedgerResponse ledger = warehouseLedgerService.approveMutation(account, request.getId());
            return ResponseBody
                    .<WarehouseLedgerResponse>builder()
                    .message("Mutation approved successfully.")
                    .data(ledger)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseLedgerResponse>builder()
                    .message("Permission denied.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (WarehouseLedgerNotFoundException e) {
            return ResponseBody
                    .<WarehouseLedgerResponse>builder()
                    .message("Ledger not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseLedgerResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/mutations/reject")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseLedgerResponse>> rejectMutation(
            @AuthenticationPrincipal Account account,
            @RequestBody ApproveRejectRequest request
    ) {
        try {
            WarehouseLedgerResponse ledger = warehouseLedgerService.rejectMutation(account, request.getId());
            return ResponseBody
                    .<WarehouseLedgerResponse>builder()
                    .message("Mutation rejected successfully.")
                    .data(ledger)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseLedgerResponse>builder()
                    .message("Permission denied.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (WarehouseLedgerNotFoundException e) {
            return ResponseBody
                    .<WarehouseLedgerResponse>builder()
                    .message("Ledger not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseLedgerResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}