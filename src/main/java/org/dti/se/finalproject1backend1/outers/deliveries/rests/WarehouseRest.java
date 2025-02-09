package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.WarehouseRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.WarehouseResponse;
import org.dti.se.finalproject1backend1.inners.usecases.warehouse.WarehouseUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<WarehouseResponse> warehouses = warehouseService
                    .getWarehouses(page, size, search);
            return ResponseBody
                    .<List<WarehouseResponse>>builder()
                    .message("Warehouses found.")
                    .data(warehouses)
                    .build()
                    .toEntity(HttpStatus.OK);
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
            @PathVariable UUID id
    ) {
        try {
            WarehouseResponse warehouse = warehouseService.getWarehouse(id);
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Warehouse found.")
                    .data(warehouse)
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
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> addWarehouse(
            @RequestBody WarehouseRequest request
    ) {
        try {
            WarehouseResponse warehouse = warehouseService.addWarehouse(request);
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
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> patchWarehouse(
            @PathVariable UUID id,
            @RequestBody WarehouseRequest request
    ) {
        try {
            WarehouseResponse warehouse = warehouseService.patchWarehouse(id, request);
            return ResponseBody
                    .<WarehouseResponse>builder()
                    .message("Warehouse patched.")
                    .data(warehouse)
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
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseResponse>> deleteWarehouse(
            @PathVariable UUID id
    ) {
        try {
            warehouseService.deleteWarehouse(id);
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
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
