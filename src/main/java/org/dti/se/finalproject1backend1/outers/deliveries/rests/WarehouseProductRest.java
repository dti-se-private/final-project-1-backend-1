package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;
import org.dti.se.finalproject1backend1.inners.usecases.warehouse.WarehouseProductUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.products.ProductNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseProductExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/warehouse-products")
public class WarehouseProductRest {
    @Autowired
    WarehouseProductUseCase warehouseProductUseCase;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<List<WarehouseProductResponse>>> getWarehouseProducts(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<WarehouseProductResponse> warehouses = warehouseProductUseCase
                    .getWarehouseProducts(account, page, size, search);
            return ResponseBody
                    .<List<WarehouseProductResponse>>builder()
                    .message("Warehouse products found.")
                    .data(warehouses)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<List<WarehouseProductResponse>>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<List<WarehouseProductResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{warehouseProductId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseProductResponse>> getWarehouseProduct(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID warehouseProductId
    ) {
        try {
            WarehouseProductResponse warehouse = warehouseProductUseCase
                    .getWarehouseProduct(account, warehouseProductId);
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Warehouse product found.")
                    .data(warehouse)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (WarehouseNotFoundException e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Warehouse not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseProductResponse>> addWarehouseProduct(
            @AuthenticationPrincipal Account account,
            @RequestBody WarehouseProductRequest request
    ) {
        try {
            WarehouseProductResponse warehouse = warehouseProductUseCase.addWarehouseProduct(account, request);
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Warehouse product added.")
                    .data(warehouse)
                    .build()
                    .toEntity(HttpStatus.CREATED);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (ProductNotFoundException e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Product not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (WarehouseNotFoundException e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Warehouse not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (WarehouseProductExistsException e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Warehouse product exists.")
                    .build()
                    .toEntity(HttpStatus.CONFLICT);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{warehouseProductId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseProductResponse>> patchWarehouseProduct(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID warehouseProductId,
            @RequestBody WarehouseProductRequest request
    ) {
        try {
            WarehouseProductResponse warehouse = warehouseProductUseCase
                    .patchWarehouseProduct(account, warehouseProductId, request);
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Warehouse product patched.")
                    .data(warehouse)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (ProductNotFoundException e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Product not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (WarehouseNotFoundException e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Warehouse not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{warehouseProductId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<WarehouseProductResponse>> deleteWarehouseProduct(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID warehouseProductId
    ) {
        try {
            warehouseProductUseCase.deleteWarehouseProduct(account, warehouseProductId);
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Warehouse product deleted.")
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (ProductNotFoundException e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Product not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (WarehouseNotFoundException e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Warehouse not found.")
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<WarehouseProductResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
