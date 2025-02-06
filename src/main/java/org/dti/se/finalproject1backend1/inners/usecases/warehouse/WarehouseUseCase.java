package org.dti.se.finalproject1backend1.inners.usecases.warehouse;

import jakarta.validation.constraints.NotNull;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.entities.Warehouse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.WarehouseRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.WarehouseResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.WarehouseCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.AccountPermissionRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
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

    @Autowired
    private AccountPermissionRepository accountPermissionRepository;

    private GeometryFactory geometryFactory = new GeometryFactory();

    public List<WarehouseResponse> getAllWarehouses(
            Account account,
            Integer page,
            Integer size,
            List<String> filters,
            String search
    ) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        return warehouseCustomRepository.getAllWarehouses(page, size, filters, search);
    }

    public WarehouseResponse getWarehouse(Account account, UUID warehouseId) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

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

    public WarehouseResponse addWarehouse(Account account, WarehouseRequest request) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setId(UUID.randomUUID());
        return getAddOrPatchWarehouseResponse(request, warehouse);
    }

    public WarehouseResponse patchWarehouse(Account account, UUID warehouseId, WarehouseRequest request) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        Warehouse warehouse = warehouseRepository
                .findById(warehouseId)
                .orElseThrow(WarehouseNotFoundException::new);

        return getAddOrPatchWarehouseResponse(request, warehouse);
    }

    public void deleteWarehouse(Account account, UUID warehouseId) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (!accountPermissions.contains("SUPER_ADMIN")) {
            throw new AccountPermissionInvalidException();
        }

        warehouseRepository.deleteById(warehouseId);
    }

    @NotNull
    private WarehouseResponse getAddOrPatchWarehouseResponse(
            WarehouseRequest request,
            Warehouse warehouse
    ) {
        warehouse.setName(request.getName());
        warehouse.setDescription(request.getDescription());
        warehouse.setLocation(geometryFactory.createPoint(new Coordinate(request.getLocation().getX(), request.getLocation().getY())));

        warehouseRepository.saveAndFlush(warehouse);

        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .description(warehouse.getDescription())
                .location(warehouse.getLocation())
                .build();
    }
}
