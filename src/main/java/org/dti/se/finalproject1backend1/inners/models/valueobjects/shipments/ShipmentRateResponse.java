package org.dti.se.finalproject1backend1.inners.models.valueobjects.shipments;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ShipmentRateResponse extends Model {
    private Boolean success;
    private String object;
    private String message;
    private Integer code;
    private ShipmentLocationResponse origin;
    private ShipmentLocationResponse destination;
    private List<ShipmentPricingResponse> pricing;
}
