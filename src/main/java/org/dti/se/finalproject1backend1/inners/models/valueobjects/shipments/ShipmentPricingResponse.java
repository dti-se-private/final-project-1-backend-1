package org.dti.se.finalproject1backend1.inners.models.valueobjects.shipments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ShipmentPricingResponse extends Model {
    private Boolean availableForCashOnDelivery;
    private Boolean availableForProofOfDelivery;
    private Boolean availableForInstantWaybillId;
    private Boolean availableForInsurance;
    private List<String> availableCollectionMethod;
    private String company;
    private String courierName;
    private String courierCode;
    private String courierServiceName;
    private String courierServiceCode;
    private String description;
    private String duration;
    private String shipmentDurationRange;
    private String shipmentDurationUnit;
    private String serviceType;
    private Double price;
    private String type;
}

