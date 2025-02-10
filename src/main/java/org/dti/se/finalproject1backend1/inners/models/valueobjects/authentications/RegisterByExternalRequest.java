package org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RegisterByExternalRequest {
    private String credential;
}