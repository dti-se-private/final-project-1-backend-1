package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.dti.se.finalproject1backend1.inners.usecases.statistics.ProductStockStatisticsUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountPermissionInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/statistics")
public class ProductStockStatisticsRest {

    @Autowired
    private ProductStockStatisticsUseCase useCase;

    @GetMapping("/product-stocks")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WAREHOUSE_ADMIN')")
    public ResponseEntity<ResponseBody<List<StatisticSeriesResponse>>> getProductStockStatistics(
            @AuthenticationPrincipal Account account,
            @RequestParam(name = "warehouse_ids", required = false) String warehouseIdsParam,
            @RequestParam(name = "operation") String operation,
            @RequestParam(name = "product_ids", required = false) String productIdsParam,
            @RequestParam(name = "aggregation") String aggregation,
            @RequestParam(name = "period") String period
    ) {
        try {
            // Inline UUID parsing
            List<UUID> warehouseIds = (warehouseIdsParam == null || warehouseIdsParam.isEmpty())
                    ? Collections.emptyList()
                    : Arrays.stream(warehouseIdsParam.split(","))
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

            List<UUID> productIds = (productIdsParam == null || productIdsParam.isEmpty())
                    ? Collections.emptyList()
                    : Arrays.stream(productIdsParam.split(","))
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

            List<StatisticSeriesResponse> data = useCase.getStatistics(
                    account, warehouseIds, productIds, operation, aggregation, period
            );

            return ResponseBody.<List<StatisticSeriesResponse>>builder()
                    .message("Statistics retrieved successfully.")
                    .data(data)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (AccountPermissionInvalidException e) {
            return ResponseBody.<List<StatisticSeriesResponse>>builder()
                    .message("Permission denied.")
                    .build()
                    .toEntity(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return ResponseBody.<List<StatisticSeriesResponse>>builder()
                    .message("Invalid parameters: " + e.getMessage())
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody.<List<StatisticSeriesResponse>>builder()
                    .message("Internal server error.")
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

