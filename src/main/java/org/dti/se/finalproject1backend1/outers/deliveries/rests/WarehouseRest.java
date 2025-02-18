package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouses.WarehouseRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouses.WarehouseResponse;
import org.dti.se.finalproject1backend1.inners.usecases.warehouse.WarehouseUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
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
@RequestMapping("/warehouses")
public class WarehouseRest {

    @Autowired
    private WarehouseUseCase warehouseUseCase;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<List<WarehouseResponse>>> getWarehouses(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<WarehouseResponse> warehouses = warehouseUseCase
                    .getWarehouses(account, page, size, search);
            return ResponseBody
                    .<List<WarehouseResponse>>builder()
                    .message("Warehouses found.")
                    .data(warehouses)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<List<WarehouseResponse>>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<List<WarehouseResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{warehouseId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> getWarehouse(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID warehouseId
    ) {
        try {
            WarehouseResponse warehouse = warehouseUseCase.getWarehouse(account, warehouseId);
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Warehouse found.")
                    .data(warehouse)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (WarehouseNotFoundException e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Warehouse not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> addWarehouse(
            @RequestBody WarehouseRequest request
    ) {
        try {
            WarehouseResponse warehouse = warehouseUseCase.addWarehouse(request);
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Warehouse added.")
                    .data(warehouse)
                    .build()
                    .toEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{warehouseId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> patchWarehouse(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID warehouseId,
            @RequestBody WarehouseRequest request
    ) {
        try {
            WarehouseResponse warehouse = warehouseUseCase.patchWarehouse(account, warehouseId, request);
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Warehouse patched.")
                    .data(warehouse)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (WarehouseNotFoundException e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Warehouse not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{warehouseId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> deleteWarehouse(
            @PathVariable UUID warehouseId
    ) {
        try {
            warehouseUseCase.deleteWarehouse(warehouseId);
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Warehouse deleted.")
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (WarehouseNotFoundException e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Warehouse not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
