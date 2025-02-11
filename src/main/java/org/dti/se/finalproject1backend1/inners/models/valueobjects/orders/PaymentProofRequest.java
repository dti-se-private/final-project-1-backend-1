package org.dti.se.finalproject1backend1.inners.models.valueobjects.orders;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PaymentProofRequest extends Model {
    private byte[] file;
    private String extension;
    private OffsetDateTime time;
}
