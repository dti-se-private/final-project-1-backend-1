package org.dti.se.finalproject1backend1.inners.usecases.statistics;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.statistics.StatisticAggregationInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.StockStatisticCustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StockStatisticUseCase {

    @Autowired
    StockStatisticCustomRepository stockStatisticCustomRepository;

    public List<StatisticSeriesResponse> getProductStockIncrement(
            Account account,
            List<UUID> productIds,
            String aggregation,
            String period
    ) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("SUPER_ADMIN")) {
            return switch (aggregation) {
                case "sum" -> stockStatisticCustomRepository.getProductStockIncrementSum(productIds, period);
                case "avg" -> stockStatisticCustomRepository.getProductStockIncrementAvg(productIds, period);
                default -> throw new StatisticAggregationInvalidException();
            };
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            return switch (aggregation) {
                case "sum" -> stockStatisticCustomRepository.getProductStockIncrementSum(account, productIds, period);
                case "avg" -> stockStatisticCustomRepository.getProductStockIncrementAvg(account, productIds, period);
                default -> throw new StatisticAggregationInvalidException();
            };
        } else {
            throw new AccountPermissionInvalidException();
        }
    }


    public List<StatisticSeriesResponse> getProductStockDecrement(
            Account account,
            List<UUID> productIds,
            String aggregation,
            String period
    ) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("SUPER_ADMIN")) {
            return switch (aggregation) {
                case "sum" -> stockStatisticCustomRepository.getProductStockDecrementSum(productIds, period);
                case "avg" -> stockStatisticCustomRepository.getProductStockDecrementAvg(productIds, period);
                default -> throw new StatisticAggregationInvalidException();
            };
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            return switch (aggregation) {
                case "sum" -> stockStatisticCustomRepository.getProductStockDecrementSum(account, productIds, period);
                case "avg" -> stockStatisticCustomRepository.getProductStockDecrementAvg(account, productIds, period);
                default -> throw new StatisticAggregationInvalidException();
            };
        } else {
            throw new AccountPermissionInvalidException();
        }
    }


    public List<StatisticSeriesResponse> getProductStockCurrent(
            Account account,
            List<UUID> productIds,
            String aggregation,
            String period
    ) {
        List<String> accountPermissions = account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();

        if (accountPermissions.contains("SUPER_ADMIN")) {
            return switch (aggregation) {
                case "sum" -> stockStatisticCustomRepository.getProductStockCurrentSum(productIds, period);
                case "avg" -> stockStatisticCustomRepository.getProductStockCurrentAvg(productIds, period);
                default -> throw new StatisticAggregationInvalidException();
            };
        } else if (accountPermissions.contains("WAREHOUSE_ADMIN")) {
            return switch (aggregation) {
                case "sum" -> stockStatisticCustomRepository.getProductStockCurrentSum(account, productIds, period);
                case "avg" -> stockStatisticCustomRepository.getProductStockCurrentAvg(account, productIds, period);
                default -> throw new StatisticAggregationInvalidException();
            };
        } else {
            throw new AccountPermissionInvalidException();
        }
    }

}