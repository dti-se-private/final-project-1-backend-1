package org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts;

import lombok.Data;

import java.util.UUID;

@Data
public class WarehouseProductRequest {
    private UUID warehouseId;
    private UUID productId;
    private Double quantity;
}
