package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.dti.se.finalproject1backend1.inners.usecases.statistics.SalesStatisticUseCase;
import org.dti.se.finalproject1backend1.inners.usecases.statistics.StockStatisticUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.statistics.StatisticAggregationInvalidException;
import org.dti.se.finalproject1backend1.outers.exceptions.statistics.StatisticOperationInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/statistics")
public class StatisticRest {
    @Autowired
    StockStatisticUseCase stockStatisticUseCase;

    @Autowired
    SalesStatisticUseCase salesStatisticUseCase;

    @GetMapping("/product-stocks")
    public ResponseEntity<ResponseBody<List<StatisticSeriesResponse>>> getProductStockStatistic(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "") List<UUID> productIds,
            @RequestParam(defaultValue = "") String operation,
            @RequestParam(defaultValue = "sum") String aggregation,
            @RequestParam(defaultValue = "day") String period
    ) {
        try {
            List<StatisticSeriesResponse> series = switch (operation) {
                case "increment" ->
                        stockStatisticUseCase.getProductStockIncrement(account, productIds, aggregation, period);
                case "decrement" ->
                        stockStatisticUseCase.getProductStockDecrement(account, productIds, aggregation, period);
                case "current" ->
                        stockStatisticUseCase.getProductStockCurrent(account, productIds, aggregation, period);
                default -> throw new StatisticOperationInvalidException();
            };
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Product stock statistic found.")
                    .data(series)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (StatisticOperationInvalidException e) {
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Operation invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (StatisticAggregationInvalidException e) {
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Aggregation invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/product-sales")
    public ResponseEntity<ResponseBody<List<StatisticSeriesResponse>>> getProductSalesStatistic(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "") List<UUID> categoryIds,
            @RequestParam(defaultValue = "") List<UUID> productIds,
            @RequestParam(defaultValue = "sum") String aggregation,
            @RequestParam(defaultValue = "day") String period
    ) {
        try {
            List<StatisticSeriesResponse> series = salesStatisticUseCase.getProductSales(
                    account,
                    categoryIds,
                    productIds,
                    aggregation,
                    period
            );
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Product sales statistic found.")
                    .data(series)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Account permission invalid.")
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (StatisticOperationInvalidException e) {
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Operation invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (StatisticAggregationInvalidException e) {
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Aggregation invalid.")
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}