package org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts;

import lombok.Data;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WarehouseProductResponse {
    private UUID id;
    private Warehouse warehouse;
    private Product product;
    private BigDecimal quantity;
}
