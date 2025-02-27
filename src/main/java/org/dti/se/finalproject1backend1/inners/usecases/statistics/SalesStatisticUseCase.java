package org.dti.se.finalproject1backend1.inners.usecases.statistics;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.statistics.StatisticAggregationInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.SalesStatisticCustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SalesStatisticUseCase {

    @Autowired
    SalesStatisticCustomRepository salesStatisticCustomRepository;

    public List<StatisticSeriesResponse> getProductSales(
            Account account,
            List<UUID> categoryIds,
            List<UUID> productIds,
            String aggregation,
            String period
    ) {
        List<String> accountPermissions = account.getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("SUPER_ADMIN")) {
            if (productIds.isEmpty()) {
                return switch (aggregation) {
                    case "sum" -> salesStatisticCustomRepository.getProductSalesSum(period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesAvg(period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            } else {
                return switch (aggregation) {
                    case "sum" -> salesStatisticCustomRepository.getProductSalesSum(productIds, period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesAvg(productIds, period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            }
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            if (productIds.isEmpty()) {
                return switch (aggregation) {
                    case "sum" -> salesStatisticCustomRepository.getProductSalesSum(account, period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesAvg(account, period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            } else {
                return switch (aggregation) {
                    case "sum" -> salesStatisticCustomRepository.getProductSalesSum(account, productIds, period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesAvg(account, productIds, period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            }
        } else {
            throw new AccountPermissionInvalidException();
        }
    }
}