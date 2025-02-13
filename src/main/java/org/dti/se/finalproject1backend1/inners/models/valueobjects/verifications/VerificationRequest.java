package org.dti.se.finalproject1backend1.inners.models.valueobjects.verifications;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class VerificationRequest extends Model {
    private String email;
    private String type;
}
