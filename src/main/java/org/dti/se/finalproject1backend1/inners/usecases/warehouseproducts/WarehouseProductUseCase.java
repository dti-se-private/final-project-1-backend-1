package org.dti.se.finalproject1backend1.inners.usecases.warehouseproducts;

import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseProduct;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseProductUseCase {
    @Autowired
    private WarehouseProductRepository warehouseProductRepository;

    @Autowired
    private WarehouseProductMapper warehouseProductMapper;

    public List<WarehouseProduct> getAllWarehouseProducts() {return warehouseProductRepository.findAll(); }

    public WarehouseProduct getWarehouseProductById(UUID id) {
        return warehouseProductRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Warehouse Product not Found for ID: " + id));
    }

    public WarehouseProduct addWarehouseProduct(WarehouseProduct warehouseProduct) {
        return warehouseProductRepository.save(warehouseProduct);
    }

    public WarehouseProductResponse updateWarehouseProduct(UUID id, WarehouseProductRequest request) {
        WarehouseProduct existing = warehouseProductRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("Warehouse Product Not Found"));
        existing.setQuantity(request.getQuantity());
        WarehouseProduct updated = warehouseProductRepository.save(existing);
        return warehouseProductMapper.toResponse(updated);
    }

    public void deleteWarehouseProduct(UUID id) {
        warehouseProductRepository.deleteById(id);
    }

}
