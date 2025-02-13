package org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class WarehouseAdminRequest {
    private UUID warehouseId;
    private UUID accountId;
}
