package org.dti.se.finalproject1backend1.inners.usecases.statistics;

import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.warehouses.WarehouseAdminNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.ProductStockStatisticsCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseAdminRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductStockStatisticsUseCase {

    @Autowired
    private ProductStockStatisticsCustomRepository customRepo;
    @Autowired
    private WarehouseRepository warehouseRepo;
    @Autowired
    private WarehouseAdminRepository warehouseAdminRepo;

    public List<StatisticSeriesResponse> getStatistics(
            Account account,
            List<UUID> warehouseIds,
            List<UUID> productIds,
            String operation,
            String aggregation,
            String period
    ) {
        List<UUID> resolvedWarehouseIds = resolveWarehouseIds(account, warehouseIds);
        validateParameters(operation, aggregation, period);

        if ("current".equalsIgnoreCase(operation)) {
            Double sum = customRepo.findCurrentStockSum(resolvedWarehouseIds, productIds);
            return Collections.singletonList(
                    new StatisticSeriesResponse()
                            .setX(OffsetDateTime.now())
                            .setY(sum != null ? sum : 0.0)
            );
        }

        return customRepo.findStockHistoryAggregates(
                resolvedWarehouseIds, productIds, operation, period
        );
    }

    private List<UUID> resolveWarehouseIds(Account account, List<UUID> requestedWarehouseIds) {
        // Check if user has SUPER_ADMIN permission
        boolean isSuperAdmin = account.getAccountPermissions().stream()
                .anyMatch(ap -> "SUPER_ADMIN".equalsIgnoreCase(ap.getPermission()));

        if (isSuperAdmin) {
            return requestedWarehouseIds.isEmpty()
                    ? warehouseRepo.findAll().stream()
                    .map(Warehouse::getId)
                    .collect(Collectors.toList())
                    : requestedWarehouseIds;
        }

        // For warehouse admins, get their assigned warehouse
        WarehouseAdmin admin = warehouseAdminRepo.findByAccount(account)
                .orElseThrow(() -> new WarehouseAdminNotFoundException(
                        "No warehouse assignment found for account: " + account.getId()));

        return List.of(admin.getWarehouse().getId());
    }

    private void validateParameters(String operation, String aggregation, String period) {
        if (!List.of("increment", "decrement", "current").contains(operation.toLowerCase())) {
            throw new IllegalArgumentException("Invalid operation");
        }
        if (!"sum".equalsIgnoreCase(aggregation)) {
            throw new IllegalArgumentException("Only sum aggregation is supported");
        }
        if (!operation.equalsIgnoreCase("current") && !List.of("day", "week", "month").contains(period.toLowerCase())) {
            throw new IllegalArgumentException("Invalid period for operation");
        }
    }
}