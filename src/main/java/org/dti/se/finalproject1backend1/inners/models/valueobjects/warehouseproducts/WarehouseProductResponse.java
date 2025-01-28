package org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WarehouseProductResponse {
    private UUID id;
    private UUID warehouseId;
    private UUID productId;
    private BigDecimal quantity;
}
