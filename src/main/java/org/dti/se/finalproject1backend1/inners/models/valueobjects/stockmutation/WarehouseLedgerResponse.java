package org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation;
import lombok.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseLedgerResponse {
    private UUID id;
    private WarehouseProductResponse warehouseProduct;
    private Double preQuantity;
    private Double postQuantity;
    private OffsetDateTime time;
    private Boolean isApproved;
}
