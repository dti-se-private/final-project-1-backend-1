package org.dti.se.finalproject1backend1.inners.models.valueobjects.orders;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class OrderResponse extends Model {
    private UUID id;

    private AccountResponse account;

    private Double itemPrice;

    private Double shipmentPrice;

    private Double totalPrice;

    private List<OrderStatusResponse> statuses;

    private List<OrderItemResponse> items;

    private List<PaymentProofResponse> paymentProofs;

    private Point shipmentOrigin;

    private Point shipmentDestination;
}
