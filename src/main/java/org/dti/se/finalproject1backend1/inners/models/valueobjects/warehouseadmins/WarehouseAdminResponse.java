package org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseadmins;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouses.WarehouseResponse;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class WarehouseAdminResponse {
    private UUID id;
    private WarehouseResponse warehouse;
    private AccountResponse account;
}
