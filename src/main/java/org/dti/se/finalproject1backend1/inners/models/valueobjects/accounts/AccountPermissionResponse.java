package org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AccountPermissionResponse {
    private UUID accountId;
    private List<String> permissions;
}
