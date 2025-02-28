package org.dti.se.finalproject1backend1.inners.models.valueobjects.stockmutation;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts.WarehouseProductResponse;

import java.time.OffsetDateTime;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class WarehouseLedgerResponse extends Model {
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
