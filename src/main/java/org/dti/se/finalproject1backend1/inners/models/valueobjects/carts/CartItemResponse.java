package org.dti.se.finalproject1backend1.inners.models.valueobjects.carts;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.products.ProductResponse;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CartItemResponse extends Model {
    private UUID id;
    private ProductResponse product;
    private Double quantity;
}
