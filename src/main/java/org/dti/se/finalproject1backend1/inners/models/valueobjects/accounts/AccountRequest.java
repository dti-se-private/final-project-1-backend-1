package org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AccountRequest extends Model {
    private String name;
    private String email;
    private String otp;
    private String password;
    private String phone;
    private byte[] image;
}
