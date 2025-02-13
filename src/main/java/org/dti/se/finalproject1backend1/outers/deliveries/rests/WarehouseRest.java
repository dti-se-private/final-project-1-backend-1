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
    private WarehouseUseCase warehouseUseCase;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<List<WarehouseResponse>>> getAllWarehouses(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") List<String> filters,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<WarehouseResponse> warehouses = warehouseUseCase
                    .getAllWarehouses(account, page, size, filters, search);
            return ResponseBody
                    .<List<WarehouseResponse>>builder()
                    .message("Warehouse fetched.")
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
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> getWarehouse(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID id
    ) {
        try {
            WarehouseResponse warehouse = warehouseUseCase.getWarehouse(account, id);
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
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> addWarehouse(
            @AuthenticationPrincipal Account account,
            @RequestBody WarehouseRequest request
    ) {
        try {
            WarehouseResponse warehouse = warehouseUseCase.addWarehouse(account, request);
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
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> patchWarehouse(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID id,
            @RequestBody WarehouseRequest request
    ) {
        try {
            WarehouseResponse warehouse = warehouseUseCase.patchWarehouse(account, id, request);
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
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> deleteWarehouse(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID id
    ) {
        try {
            warehouseUseCase.deleteWarehouse(account, id);
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
