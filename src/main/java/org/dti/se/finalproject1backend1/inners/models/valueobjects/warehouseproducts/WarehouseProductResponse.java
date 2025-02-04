package org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.entities.Product;
import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class WarehouseProductResponse {
    private UUID id;
    private Warehouse warehouse;
    private Product product;
    private Double quantity;
}
