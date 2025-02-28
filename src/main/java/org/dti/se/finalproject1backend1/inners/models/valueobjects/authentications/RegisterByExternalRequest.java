package org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RegisterByExternalRequest extends Model {
    private String credential;
}