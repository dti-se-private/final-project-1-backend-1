package org.dti.se.finalproject1backend1.inners.usecases.statistics;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.WarehouseAdmin;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.ProductSalesStatisticsCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductSalesStatisticsUseCase {

    @Autowired
    private ProductSalesStatisticsCustomRepository productSalesStatisticsCustomRepository;

    @Autowired
    private WarehouseAdminRepository warehouseAdminRepository;

    public List<StatisticSeriesResponse> getProductSalesStatistics(
            Account account,
            List<String> warehouseIds,
            List<String> categoryIds,
            List<String> productIds,
            String aggregation,
            String period
    ) {
        // Check if the account has SUPER_ADMIN permission
        if (account.getAccountPermissions().stream().noneMatch(p -> p.getPermission().equals("SUPER_ADMIN"))) {
            // If not SUPER_ADMIN, check if the account has WAREHOUSE_ADMIN permission
            if (account.getAccountPermissions().stream().noneMatch(p -> p.getPermission().equals("WAREHOUSE_ADMIN"))) {
                throw new AccountPermissionInvalidException();
            }

            // For WAREHOUSE_ADMIN, fetch the assigned warehouse from the WarehouseAdmin entity
            Optional<WarehouseAdmin> warehouseAdminOpt = warehouseAdminRepository.findById(account.getId());
            if (warehouseAdminOpt.isEmpty()) {
                throw new AccountPermissionInvalidException("Warehouse Admin is not assigned to any warehouse.");
            }

            // Set the warehouse ID to the assigned warehouse
            warehouseIds = List.of(warehouseAdminOpt.get().getWarehouse().getId().toString());
        }

        return productSalesStatisticsCustomRepository.getProductSalesStatistics(
                warehouseIds, categoryIds, productIds, aggregation, period);
    }
}