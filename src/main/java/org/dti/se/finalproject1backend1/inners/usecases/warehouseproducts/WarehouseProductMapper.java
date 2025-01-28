package org.dti.se.finalproject1backend1.inners.usecases.warehouseproducts;

import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseProduct;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;

import java.util.UUID;

public class WarehouseProductMapper {

    public WarehouseProductResponse toResponse(WarehouseProduct entity) {
        if (entity == null) {
            return null;
        }

        WarehouseProductResponse response = new WarehouseProductResponse();
        response.setWarehouseId(entity.getWarehouse().getId());
        response.setProductId(entity.getProduct().getId());
        response.setQuantity(entity.getQuantity());
        return response;
    }

    public WarehouseProduct toEntity(WarehouseProductRequest dto) {
        if (dto == null) {
            return null;
        }

        WarehouseProduct entity = new WarehouseProduct();
        entity.setWarehouse(toWarehouse(dto.getWarehouseId()));
        entity.setProduct(toProduct(dto.getProductId()));
        entity.setQuantity(dto.getQuantity());
        return entity;
    }

    private Warehouse toWarehouse(UUID id) {
        if (id == null) {
            return null;
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        return warehouse;
    }

    private Product toProduct(UUID id) {
        if (id == null) {
            return null;
        }
        Product product = new Product();
        product.setId(id);
        return product;
    }
}
