package org.dti.se.finalproject1backend1.inners.usecases.statistics;

import lombok.RequiredArgsConstructor;
import org.dti.se.finalproject1backend1.outers.repositories.ones.WarehouseLedgerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockStatisticsUseCase {
//    private final WarehouseLedgerRepository warehouseLedgerRepository;
////    private final AuthService authService; // To get user role and warehouse access
//
//    public List<Map<String, Object>> getProductStockStatistics(
//            List<UUID> warehouseIds,
//            List<UUID> productIds,
//            String period
//    ) {
//        OffsetDateTime now = OffsetDateTime.now();
//        OffsetDateTime startDate = switch (period.toLowerCase()) {
//            case "day" -> now.truncatedTo(ChronoUnit.DAYS);
//            case "week" -> now.minusWeeks(1).truncatedTo(ChronoUnit.DAYS);
//            case "month" -> now.minusMonths(1).truncatedTo(ChronoUnit.DAYS);
//            default -> throw new IllegalArgumentException("Invalid period: Use 'day', 'week', or 'month'.");
//        };
//
//        List<Object[]> results = warehouseLedgerRepository.getProductStockStatistics(
//                warehouseIds, productIds, startDate, now
//        );
//
////        return results.stream()
////                .map(result -> Map.of(
////                        "x", ((result[1] instanceof OffsetDateTime)
////                                ? ((OffsetDateTime) result[1]).toEpochSecond()
////                                : ((Timestamp) result[1]).toInstant().getEpochSecond()),
////                        "y", ((result[0] instanceof Number)
////                                ? ((Number) result[0]).doubleValue()
////                                : new BigDecimal(result[0].toString()).doubleValue())
////                ))
////                .toList();
//    }
}
