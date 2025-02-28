package org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseadmins;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouses.WarehouseResponse;

import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class WarehouseAdminResponse extends Model {
    private UUID id;
    private WarehouseResponse warehouse;
    private AccountResponse account;
}
