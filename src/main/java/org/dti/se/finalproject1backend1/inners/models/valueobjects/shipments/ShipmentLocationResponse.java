package org.dti.se.finalproject1backend1.inners.models.valueobjects.shipments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ShipmentLocationResponse extends Model {
    private String locationId;
    private Double latitude;
    private Double longitude;
    private Integer postalCode;
    private String countryName;
    private String countryCode;
    private String administrativeDivisionLevel1Name;
    private String administrativeDivisionLevel1Type;
    private String administrativeDivisionLevel2Name;
    private String administrativeDivisionLevel2Type;
    private String administrativeDivisionLevel3Name;
    private String administrativeDivisionLevel3Type;
    private String administrativeDivisionLevel4Name;
    private String administrativeDivisionLevel4Type;
    private String address;
}

