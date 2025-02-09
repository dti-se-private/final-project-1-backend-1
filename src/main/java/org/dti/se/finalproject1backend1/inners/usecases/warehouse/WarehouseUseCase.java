package org.dti.se.finalproject1backend1.inners.usecases.warehouse;

import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.WarehouseRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.WarehouseResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.WarehouseCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WarehouseUseCase {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private WarehouseCustomRepository warehouseCustomRepository;

    public List<WarehouseResponse> getWarehouses(
            Integer page,
            Integer size,
            String search
    ) {
        return warehouseCustomRepository.getWarehouses(page, size, search);
    }

    public WarehouseResponse getWarehouse(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository
                .findById(warehouseId)
                .orElseThrow(WarehouseNotFoundException::new);

        WarehouseResponse warehouseResponse = new WarehouseResponse();
        warehouseResponse.setId(warehouse.getId());
        warehouseResponse.setName(warehouse.getName());
        warehouseResponse.setDescription(warehouse.getDescription());
        warehouseResponse.setLocation(warehouse.getLocation());

        return warehouseResponse;
    }

    public WarehouseResponse addWarehouse(WarehouseRequest request) {
        Warehouse warehouse = Warehouse
                .builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .build();

        Warehouse savedWarehouse = warehouseRepository.saveAndFlush(warehouse);

        return WarehouseResponse.builder()
                .id(savedWarehouse.getId())
                .name(savedWarehouse.getName())
                .description(savedWarehouse.getDescription())
                .location(savedWarehouse.getLocation())
                .build();
    }

    public WarehouseResponse patchWarehouse(UUID warehouseId, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository
                .findById(warehouseId)
                .orElseThrow(WarehouseNotFoundException::new);

        warehouse
                .setName(request.getName())
                .setDescription(request.getDescription())
                .setLocation(request.getLocation());

        Warehouse patchedWarehouse = warehouseRepository.saveAndFlush(warehouse);

        return WarehouseResponse.builder()
                .id(patchedWarehouse.getId())
                .name(patchedWarehouse.getName())
                .description(patchedWarehouse.getDescription())
                .location(patchedWarehouse.getLocation())
                .build();
    }

    public void deleteWarehouse(UUID warehouseId) {
        warehouseRepository.deleteById(warehouseId);
    }
}
