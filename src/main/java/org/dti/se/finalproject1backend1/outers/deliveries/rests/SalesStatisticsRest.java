package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.dti.se.finalproject1backend1.inners.usecases.statistics.SalesStatisticsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/statistics/product-sales")
@RequiredArgsConstructor
public class SalesStatisticsRest {
//    private final SalesStatisticsUseCase salesStatisticsService;
//
////    @GetMapping
////    public ResponseEntity<List<Map<String, Object>>> getProductSalesStatistics(
////            Authentication authentication,
////            @RequestParam(required = false) List<UUID> warehouse_ids,
////            @RequestParam(required = false) List<UUID> category_ids,
////            @RequestParam(required = false) List<UUID> product_ids,
////            @RequestParam String aggregation,
////            @RequestParam String period) {
////
////        List<Map<String, Object>> statistics = salesStatisticsService.getStatistics(authentication, warehouse_ids, category_ids, product_ids, aggregation, period);
////        return ResponseEntity.ok(statistics);
////    }
//    @GetMapping("/product/{productId}")
//    public double getTotalSalesByProduct(@PathVariable UUID productId, @RequestParam OffsetDateTime startDate, @RequestParam OffsetDateTime endDate) {
//        return salesStatisticsService.getTotalSalesByProduct(productId, startDate, endDate);
//    }
//
//    @GetMapping("/category/{categoryId}")
//    public double getTotalSalesByCategory(@PathVariable UUID categoryId, @RequestParam OffsetDateTime startDate, @RequestParam OffsetDateTime endDate) {
//        return salesStatisticsService.getTotalSalesByCategory(categoryId, startDate, endDate);
//    }
//
//    @GetMapping("/total")
//    public double getTotalSales(@RequestParam OffsetDateTime startDate, @RequestParam OffsetDateTime endDate) {
//        return salesStatisticsService.getTotalSales(startDate, endDate);
//    }
//
//    @GetMapping("/period/{period}")
//    public double getTotalSalesByPeriod(@PathVariable String period) {
//        return salesStatisticsService.getTotalSalesByPeriod(period);
//    }
}

