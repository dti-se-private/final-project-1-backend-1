package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseadmins.WarehouseAdminRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseadmins.WarehouseAdminResponse;
import org.dti.se.finalproject1backend1.inners.usecases.warehouse.WarehouseAdminManagementUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseAdminExistsException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseAdminNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/warehouse-admins")
public class WarehouseAdminRest {

    @Autowired
    private WarehouseAdminManagementUseCase warehouseAdminManagementUseCase;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<List<WarehouseAdminResponse>>> getWarehouseAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<WarehouseAdminResponse> warehouseAdmins = warehouseAdminManagementUseCase
                    .getWarehouseAdmins(page, size, search);
            return ResponseBody
                    .<List<WarehouseAdminResponse>>builder()
                    .message("Warehouse admins found.")
                    .data(warehouseAdmins)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<List<WarehouseAdminResponse>>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<List<WarehouseAdminResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{warehouseAdminId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseAdminResponse>> getWarehouseAdmin(
            @PathVariable UUID warehouseAdminId
    ) {
        try {
            WarehouseAdminResponse warehouseAdmin = warehouseAdminManagementUseCase
                    .getWarehouseAdmin(warehouseAdminId);
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
                    .toEntity(HttpStatus.BAD_REQUEST);
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
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseAdminResponse>> addWarehouseAdmin(
            @RequestBody WarehouseAdminRequest warehouseAdminRequest
    ) {
        try {
            WarehouseAdminResponse warehouseAdmin = warehouseAdminManagementUseCase
                    .addWarehouseAdmin(warehouseAdminRequest);
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Warehouse admin added.")
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
        } catch (WarehouseAdminExistsException e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Warehouse admin exists.")
                    .build()
                    .toEntity(HttpStatus.CONFLICT);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{warehouseAdminId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseAdminResponse>> patchWarehouseAdmin(
            @PathVariable UUID warehouseAdminId,
            @RequestBody WarehouseAdminRequest warehouseAdminRequest
    ) {
        try {
            WarehouseAdminResponse warehouseAdmin = warehouseAdminManagementUseCase
                    .patchWarehouseAdmin(warehouseAdminId, warehouseAdminRequest);
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Warehouse admin patched.")
                    .data(warehouseAdmin)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseAdminResponse>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
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

    @DeleteMapping("/{warehouseAdminId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<Void>> deleteWarehouseAdmin(
            @PathVariable UUID warehouseAdminId
    ) {
        try {
            warehouseAdminManagementUseCase.deleteWarehouseAdmin(warehouseAdminId);
            return ResponseBody
                    .<Void>builder()
                    .message("Warehouse admin deleted.")
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
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
