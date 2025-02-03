package org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ResetPasswordRequest {
    private String email;
    private String otp;
    private String newPassword;
}
