package org.dti.se.finalproject1backend1.inners.models.valueobjects.mutations;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddMutationRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Origin warehouse ID is required")
    private UUID originWarehouseId;

    @NotNull(message = "Destination warehouse ID is required")
    private UUID destinationWarehouseId;

    private Double quantity;
}
