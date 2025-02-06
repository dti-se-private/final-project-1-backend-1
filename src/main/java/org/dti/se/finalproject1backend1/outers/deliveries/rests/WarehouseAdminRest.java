package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.admin.WarehouseAdminRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.admin.WarehouseAdminResponse;
import org.dti.se.finalproject1backend1.inners.usecases.warehouse.admin.WarehouseAdminManagementUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseAdminAndWarehouseDuplicateException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseAdminNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/warehouse-admins")
public class WarehouseAdminRest {

    @Autowired
    private WarehouseAdminManagementUseCase warehouseAdminManagementUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseBody<ResponseEntity<List<WarehouseAdminResponse>>> getAllWarehouseAdmins(
            @AuthenticationPrincipal Account authenticatedAccount,
            Integer page,
            Integer size,
            List<String> filters,
            String search
    ) {
        try {
            List<WarehouseAdminResponse> warehouseAdmins = warehouseAdminManagementUseCase
                    .getAllWarehouseAdmins(authenticatedAccount, page, size, filters, search);

            return ResponseBody
                    .<ResponseEntity<List<WarehouseAdminResponse>>>builder()
                    .message("Warehouse admins found.")
                    .data(ResponseEntity.ok(warehouseAdmins))
                    .build();
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<ResponseEntity<List<WarehouseAdminResponse>>>builder()
                    .message("Account permission invalid.")
                    .build();
        } catch (Exception e) {
            return ResponseBody
                    .<ResponseEntity<List<WarehouseAdminResponse>>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseAdminResponse>> getWarehouseAdminById(
            @AuthenticationPrincipal Account authenticatedAccount,
            @PathVariable UUID id
    ) {
        try {
            WarehouseAdminResponse warehouseAdmin = warehouseAdminManagementUseCase
                    .getWarehouseAdmin(authenticatedAccount, id);

            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Warehouse admin found.")
                    .data(warehouseAdmin)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (WarehouseAdminNotFoundException e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Warehouse admin not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseAdminResponse>> assignWarehouseAdmin(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestBody WarehouseAdminRequest warehouseAdminRequest
    ) {
        try {
            WarehouseAdminResponse warehouseAdmin = warehouseAdminManagementUseCase
                    .assignWarehouseAdmin(authenticatedAccount, warehouseAdminRequest);

            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Warehouse admin assigned.")
                    .data(warehouseAdmin)
                    .build()
                    .toEntity(HttpStatus.CREATED);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Account not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (WarehouseNotFoundException e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Warehouse not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (WarehouseAdminAndWarehouseDuplicateException e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Warehouse admin and warehouse already assigned.")
                    .build()
                    .toEntity(HttpStatus.CONFLICT);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseAdminResponse>> updateWarehouseAdmin(
            @AuthenticationPrincipal Account authenticatedAccount,
            @PathVariable UUID id,
            @RequestBody WarehouseAdminRequest warehouseAdminRequest
    ) {
        try {
            WarehouseAdminResponse warehouseAdmin = warehouseAdminManagementUseCase
                    .updateWarehouseAdmin(authenticatedAccount, id, warehouseAdminRequest);

            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Warehouse admin updated.")
                    .data(warehouseAdmin)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (WarehouseAdminNotFoundException e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Warehouse admin not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (AccountNotFoundException e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Account not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (WarehouseNotFoundException e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Warehouse not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<Void>> deleteWarehouseAdmin(
            @AuthenticationPrincipal Account authenticatedAccount,
            @PathVariable UUID id
    ) {
        try {
            warehouseAdminManagementUseCase.deleteWarehouseAdmin(authenticatedAccount, id);
            return ResponseBody
                    .<Void>builder()
                    .message("Warehouse admin deleted.")
                    .build()
                    .toEntity(HttpStatus.NO_CONTENT);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (WarehouseAdminNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Warehouse admin not found.")
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
}
