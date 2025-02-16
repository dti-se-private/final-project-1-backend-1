package org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation;
import lombok.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.WarehouseResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseLedgerResponse {
    private UUID id;
    private ProductResponse product;
    private WarehouseResponse originWarehouse;
    private WarehouseResponse destinationWarehouse;
    private Double originPreQuantity;
    private Double originPostQuantity;
    private Double destinationPreQuantity;
    private Double destinationPostQuantity;
    private OffsetDateTime time;
    private String status;
}
