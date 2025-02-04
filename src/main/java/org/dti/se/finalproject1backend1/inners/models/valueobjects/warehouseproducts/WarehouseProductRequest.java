package org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class WarehouseProductRequest {
    private UUID warehouseId;
    private UUID productId;
    private Double quantity;
}
