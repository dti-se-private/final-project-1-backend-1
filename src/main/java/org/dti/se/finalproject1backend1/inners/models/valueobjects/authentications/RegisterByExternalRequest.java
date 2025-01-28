package org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RegisterByExternalRequest {
    private String idToken;
}