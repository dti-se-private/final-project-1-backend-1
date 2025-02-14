package org.dti.se.finalproject1backend1.inners.usecases.warehouseproducts;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.WarehouseRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.WarehouseResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.WarehouseProductCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.ProductRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseProductRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseRepository;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseProductUseCase {
    @Autowired
    private WarehouseProductRepository warehouseProductRepository;

    @Autowired
    private WarehouseProductCustomRepository warehouseProductCustomRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<WarehouseProductResponse> getAllWarehouseProducts(
            Account account,
            Integer page,
            Integer size,
            List<String> filters,
            String search
    ) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        return warehouseProductCustomRepository.getAllWarehouseProducts(page, size, filters, search);
    }

    public WarehouseProductResponse getWarehouseProduct(Account account, UUID warehouseProductId) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        WarehouseProduct warehouseProduct = warehouseProductRepository
                .findById(warehouseProductId)
                .orElseThrow(EntityNotFoundException::new);

        Warehouse foundWarehouse = warehouseRepository
                .findById(warehouseProduct.getWarehouse().getId())
                .orElseThrow(EntityNotFoundException::new);

        Product foundProduct = productRepository
                .findById(warehouseProduct.getProduct().getId())
                .orElseThrow(EntityNotFoundException::new);

        WarehouseProductResponse warehouseProductResponse = new WarehouseProductResponse();
        warehouseProductResponse.setId(warehouseProduct.getId());
        warehouseProductResponse.setWarehouse(WarehouseResponse.builder()
                .id(foundWarehouse.getId())
                .name(foundWarehouse.getName())
                .description(foundWarehouse.getDescription())
                .location(foundWarehouse.getLocation())
                .build());
        warehouseProductResponse.setProduct(ProductResponse.builder()
                .id(foundProduct.getId())
                .name(foundProduct.getName())
                .description(foundProduct.getDescription())
                .price(foundProduct.getPrice())
                .image(foundProduct.getImage())
                .category(CategoryResponse.builder()
                        .id(foundProduct.getCategory().getId())
                        .name(foundProduct.getCategory().getName())
                        .description(foundProduct.getCategory().getDescription())
                        .build())
                .build());
        warehouseProductResponse.setQuantity(warehouseProduct.getQuantity());

        return warehouseProductResponse;
    }

    public WarehouseProductResponse addWarehouseProduct(Account account, WarehouseProductRequest request) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        Warehouse warehouse = warehouseRepository
                .findById(request.getWarehouseId())
                .orElseThrow(EntityNotFoundException::new);

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(EntityNotFoundException::new);

        WarehouseProduct warehouseProduct = new WarehouseProduct();
        warehouseProduct.setId(UUID.randomUUID());
        warehouseProduct.setWarehouse(warehouse);
        warehouseProduct.setProduct(product);
        warehouseProduct.setQuantity(request.getQuantity());

        warehouseProductRepository.saveAndFlush(warehouseProduct);

        return WarehouseProductResponse.builder()
                .id(warehouseProduct.getId())
                .warehouse(WarehouseResponse.builder()
                        .id(warehouse.getId())
                        .name(warehouse.getName())
                        .description(warehouse.getDescription())
                        .location(warehouse.getLocation())
                        .build())
                .product(ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .image(product.getImage())
                        .category(CategoryResponse.builder()
                                .id(product.getCategory().getId())
                                .name(product.getCategory().getName())
                                .description(product.getCategory().getDescription())
                                .build())
                        .build())
                .quantity(warehouseProduct.getQuantity())
                .build();
    }

    public WarehouseProductResponse patchWarehouseProduct(Account account, UUID warehouseProductId, WarehouseProductRequest request) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        WarehouseProduct warehouseProduct = warehouseProductRepository
                .findById(warehouseProductId)
                .orElseThrow(EntityNotFoundException::new);

        Warehouse warehouse = warehouseRepository
                .findById(request.getWarehouseId())
                .orElseThrow(EntityNotFoundException::new);

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(EntityNotFoundException::new);

        warehouseProduct.setWarehouse(warehouse);
        warehouseProduct.setProduct(product);
        warehouseProduct.setQuantity(request.getQuantity());

        warehouseProductRepository.saveAndFlush(warehouseProduct);

        return WarehouseProductResponse.builder()
                .id(warehouseProduct.getId())
                .warehouse(WarehouseResponse.builder()
                        .id(warehouse.getId())
                        .name(warehouse.getName())
                        .description(warehouse.getDescription())
                        .location(warehouse.getLocation())
                        .build())
                .product(ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .image(product.getImage())
                        .category(CategoryResponse.builder()
                                .id(product.getCategory().getId())
                                .name(product.getCategory().getName())
                                .description(product.getCategory().getDescription())
                                .build())
                        .build())
                .quantity(warehouseProduct.getQuantity())
                .build();

    }

    public void deleteWarehouseProduct(Account account, UUID warehouseProductId) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        warehouseProductRepository.deleteById(warehouseProductId);
    }
}
