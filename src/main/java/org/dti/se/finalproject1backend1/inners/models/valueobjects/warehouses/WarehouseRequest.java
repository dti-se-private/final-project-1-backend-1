package org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouses;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;
import org.locationtech.jts.geom.Point;


@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class WarehouseRequest extends Model {
    private String name;
    private String description;
    private Point location;
}
