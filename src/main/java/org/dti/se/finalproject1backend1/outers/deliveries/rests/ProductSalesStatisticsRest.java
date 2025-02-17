package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.dti.se.finalproject1backend1.inners.usecases.statistics.ProductSalesStatisticsUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statistics")
public class ProductSalesStatisticsRest {

    @Autowired
    private ProductSalesStatisticsUseCase productSalesStatisticsUseCase;

    @GetMapping("/product-sales")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<List<StatisticSeriesResponse>>> getProductSalesStatistics(
            @AuthenticationPrincipal Account account,
            @RequestParam(required = false) List<String> warehouseIds,
            @RequestParam(required = false) List<String> categoryIds,
            @RequestParam(required = false) List<String> productIds,
            @RequestParam(defaultValue = "sum") String aggregation,
            @RequestParam(defaultValue = "day") String period
    ) {
        try {
            List<StatisticSeriesResponse> salesData = productSalesStatisticsUseCase.getProductSalesStatistics(
                    account, warehouseIds, categoryIds, productIds, aggregation, period);
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Product sales statistics retrieved successfully.")
                    .data(salesData)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("You don't have permission to access this resource.")
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Internal server error.")
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}