package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseProduct;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;
import org.dti.se.finalproject1backend1.inners.usecases.warehouseproducts.WarehouseProductMapper;
import org.dti.se.finalproject1backend1.inners.usecases.warehouseproducts.WarehouseProductUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/warehouse-products")
@RequiredArgsConstructor
public class WarehouseProductRest {
    @Autowired
    private WarehouseProductUseCase warehouseProductService;

    @Autowired
    private final WarehouseProductMapper warehouseProductMapper;

    @GetMapping
    public List<WarehouseProductResponse> listAllWarehouseProducts() {
        return warehouseProductService.getAllWarehouseProducts().stream().map(warehouseProductMapper::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public WarehouseProductResponse getWarehouseProductById(@PathVariable UUID id) {
        WarehouseProduct warehouseProduct = warehouseProductService.getWarehouseProductById(id);
        return warehouseProductMapper.toResponse(warehouseProduct);
    }

    @PostMapping
    public WarehouseProductResponse addWarehouseProduct(@RequestBody WarehouseProductRequest request) {
        WarehouseProduct entity = warehouseProductMapper.toEntity(request);
        WarehouseProduct saved = warehouseProductService.addWarehouseProduct(entity);
        return warehouseProductMapper.toResponse(saved);
    }

    @PutMapping("/{id}")
    public WarehouseProductResponse updateWarehouseProduct(@PathVariable UUID id,@RequestBody WarehouseProductRequest request) {
        return warehouseProductService.updateWarehouseProduct(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable UUID id) {
        warehouseProductService.deleteWarehouseProduct(id);
    }
}
