package org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouseadmins;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;

import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class WarehouseAdminRequest extends Model {
    private UUID warehouseId;
    private UUID accountId;
}
