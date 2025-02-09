package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.orders.OrderResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.WarehouseRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.WarehouseResponse;
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
    private WarehouseUseCase warehouseService;

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<List<WarehouseResponse>>> getAllWarehouses(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<String> filters,
            @RequestParam(required = false) String search
    ) {
        try {
            List<WarehouseResponse> warehouses = warehouseService
                    .getAllWarehouses(account, page, size, filters, search);
            return ResponseBody
                    .<List<WarehouseResponse>>builder()
                    .message("Orders found.")
                    .data(warehouses)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<List<WarehouseResponse>>builder()
                    .message("You don't have permission to access this resource.")
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return ResponseBody
                    .<List<WarehouseResponse>>builder()
                    .message("Internal server error.")
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> getWarehouse(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID id
    ) {
        try {
            WarehouseResponse warehouse = warehouseService.getWarehouse(account, id);
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Warehouse found.")
                    .data(warehouse)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("You don't have permission to access this resource.")
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
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
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> addWarehouse(
            @AuthenticationPrincipal Account account,
            @RequestBody WarehouseRequest request
    ) {
        try {
            WarehouseResponse warehouse = warehouseService.addWarehouse(account, request);
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Warehouse added.")
                    .data(warehouse)
                    .build()
                    .toEntity(HttpStatus.CREATED);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("You don't have permission to access this resource.")
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Internal server error.")
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> patchWarehouse(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID id,
            @RequestBody WarehouseRequest request
    ) {
        try {
            WarehouseResponse warehouse = warehouseService.patchWarehouse(account, id, request);
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Warehouse patched.")
                    .data(warehouse)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("You don't have permission to access this resource.")
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
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
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> deleteWarehouse(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID id
    ) {
        try {
            warehouseService.deleteWarehouse(account, id);
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Warehouse deleted.")
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("You don't have permission to access this resource.")
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
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
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
