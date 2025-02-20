package org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation;
import lombok.*;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseLedger;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;


import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseLedgerResponse {
    private UUID id;
    private ProductResponse product;
    private WarehouseLedger originWarehouse;
    private WarehouseLedger destinationWarehouse;
    private Double originPreQuantity;
    private Double originPostQuantity;
    private Double destinationPreQuantity;
    private Double destinationPostQuantity;
    private OffsetDateTime time;
    private String status;
}
