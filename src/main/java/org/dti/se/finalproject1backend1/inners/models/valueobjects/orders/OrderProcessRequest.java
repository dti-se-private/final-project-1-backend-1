package org.dti.se.finalproject1backend1.inners.models.valueobjects.orders;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class OrderProcessRequest extends Model {
    private UUID orderId;
    private String action;
}
