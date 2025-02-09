package org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.locationtech.jts.geom.Point;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AccountAddressRequest {
    private String name;
    private String address;
    private Boolean isPrimary;
    private Point location;
}
