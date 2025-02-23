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

    public List<StatisticSeriesResponse> getProductSalesIncrement(
            Account account,
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
                    case "sum" -> salesStatisticCustomRepository.getProductSalesIncrementSum(period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesIncrementAvg(period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            } else {
                return switch (aggregation) {
                    case "sum" -> salesStatisticCustomRepository.getProductSalesIncrementSum(productIds, period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesIncrementAvg(productIds, period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            }
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            if (productIds.isEmpty()) {
                return switch (aggregation) {
                    case "sum" -> salesStatisticCustomRepository.getProductSalesIncrementSum(account, period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesIncrementAvg(account, period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            } else {
                return switch (aggregation) {
                    case "sum" -> salesStatisticCustomRepository.getProductSalesIncrementSum(account, productIds, period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesIncrementAvg(account, productIds, period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            }
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

    public List<StatisticSeriesResponse> getProductSalesDecrement(
            Account account,
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
                    case "sum" -> salesStatisticCustomRepository.getProductSalesDecrementSum(period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesDecrementAvg(period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            } else {
                return switch (aggregation) {
                    case "sum" -> salesStatisticCustomRepository.getProductSalesDecrementSum(productIds, period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesDecrementAvg(productIds, period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            }
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            if (productIds.isEmpty()) {
                return switch (aggregation) {
                    case "sum" -> salesStatisticCustomRepository.getProductSalesDecrementSum(account, period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesDecrementAvg(account, period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            } else {
                return switch (aggregation) {
                    case "sum" -> salesStatisticCustomRepository.getProductSalesDecrementSum(account, productIds, period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesDecrementAvg(account, productIds, period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            }
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

    public List<StatisticSeriesResponse> getProductSalesCurrent(
            Account account,
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
                    case "sum" -> salesStatisticCustomRepository.getProductSalesCurrentSum(period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesCurrentAvg(period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            } else {
                return switch (aggregation) {
                    case "sum" -> salesStatisticCustomRepository.getProductSalesCurrentSum(productIds, period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesCurrentAvg(productIds, period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            }
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            if (productIds.isEmpty()) {
                return switch (aggregation) {
                    case "sum" -> salesStatisticCustomRepository.getProductSalesCurrentSum(account, period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesCurrentAvg(account, period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            } else {
                return switch (aggregation) {
                    case "sum" -> salesStatisticCustomRepository.getProductSalesCurrentSum(account, productIds, period);
                    case "avg" -> salesStatisticCustomRepository.getProductSalesCurrentAvg(account, productIds, period);
                    default -> throw new StatisticAggregationInvalidException();
                };
            }
        } else {
            throw new AccountPermissionInvalidException();
        }
    }
}