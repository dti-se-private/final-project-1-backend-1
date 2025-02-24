package org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseLedgerResponse {
    private UUID id;
    private WarehouseProductResponse originWarehouseProduct;
    private WarehouseProductResponse destinationWarehouseProduct;
    private Double originPreQuantity;
    private Double originPostQuantity;
    private Double destinationPreQuantity;
    private Double destinationPostQuantity;
    private OffsetDateTime time;
    private String status;
}
