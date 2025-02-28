package org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseproducts;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouses.WarehouseResponse;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class WarehouseProductResponse extends Model {
    private UUID id;
    private WarehouseResponse warehouse;
    private ProductResponse product;
    private Double quantity;
}
