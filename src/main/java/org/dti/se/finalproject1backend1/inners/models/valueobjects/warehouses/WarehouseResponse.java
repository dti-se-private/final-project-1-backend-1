package org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class WarehouseResponse {
    private UUID id;
    private String name;
    private String description;
    private Point location;

}
