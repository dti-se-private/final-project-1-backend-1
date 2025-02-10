package org.dti.se.finalproject1backend1.inners.usecases.warehouseproducts;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseProduct;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseProductUseCase {
    @Autowired
    private WarehouseProductRepository warehouseProductRepository;

    @Autowired
    private WarehouseProductMapper warehouseProductMapper;

    public Page<WarehouseProductResponse> getAllWarehouseProducts(Pageable pageable, String filters, String search) {
        Page<WarehouseProduct> warehouseProducts;

        if (search != null && !search.isEmpty()) {
            warehouseProducts = warehouseProductRepository.searchByProductOrWarehouse(search, pageable);
        } else if (filters != null && !filters.isEmpty()) {
            warehouseProducts = warehouseProductRepository.findWithFilters(filters, pageable);
        } else {
            warehouseProducts = warehouseProductRepository.findAll(pageable);
        }
        return warehouseProducts.map(warehouseProductMapper::toResponse);
    }

    public WarehouseProduct getWarehouseProductById(UUID id) {
        return warehouseProductRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse Product not Found for ID: " + id));
    }

    public WarehouseProduct addWarehouseProduct(WarehouseProduct warehouseProduct) {
        return warehouseProductRepository.saveAndFlush(warehouseProduct);
    }

    public WarehouseProductResponse updateWarehouseProduct(UUID id, WarehouseProductRequest request) {
        WarehouseProduct existing = warehouseProductRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse Product Not Found"));
        existing.setQuantity(request.getQuantity());
        WarehouseProduct updated = warehouseProductRepository.saveAndFlush(existing);
        return warehouseProductMapper.toResponse(updated);
    }

    public void deleteWarehouseProduct(UUID id) {
        warehouseProductRepository.deleteById(id);
    }

}
