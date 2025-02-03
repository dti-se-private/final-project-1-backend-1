package org.dti.se.finalproject1backend1.inners.models.valueobjects;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Session extends Model {
    private AccountResponse account;
    private String accessToken;
    private String refreshToken;
    private OffsetDateTime accessTokenExpiredAt;
    private OffsetDateTime refreshTokenExpiredAt;
    private List<String> permissions;
}
