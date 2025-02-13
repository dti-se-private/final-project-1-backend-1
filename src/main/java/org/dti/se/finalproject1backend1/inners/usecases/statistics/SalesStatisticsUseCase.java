package org.dti.se.finalproject1backend1.inners.usecases.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SalesStatisticsUseCase {
//    private final WarehouseLedgerRepository warehouseLedgerRepository;
//
//    public double getTotalSalesByProduct(UUID productId, OffsetDateTime startDate, OffsetDateTime endDate) {
//        List<WarehouseLedger> ledgers = warehouseLedgerRepository.findByProductIdAndTimeBetween(productId, startDate, endDate);
//        return ledgers.stream()
//                .mapToDouble(ledger -> (ledger.getPreQuantity() - ledger.getPostQuantity()) * ledger.getProduct().getPrice())
//                .sum();
//    }
//
//    public double getTotalSalesByCategory(UUID categoryId, OffsetDateTime startDate, OffsetDateTime endDate) {
//        List<WarehouseLedger> ledgers = warehouseLedgerRepository.findByCategoryIdAndTimeBetween(categoryId, startDate, endDate);
//        return ledgers.stream()
//                .mapToDouble(ledger -> (ledger.getPreQuantity() - ledger.getPostQuantity()) * ledger.getProduct().getPrice())
//                .sum();
//    }
//
//    public double getTotalSales(OffsetDateTime startDate, OffsetDateTime endDate) {
//        List<WarehouseLedger> ledgers = warehouseLedgerRepository.findByTimeBetween(startDate, endDate);
//        return ledgers.stream()
//                .mapToDouble(ledger -> (ledger.getPreQuantity() - ledger.getPostQuantity()) * ledger.getProduct().getPrice())
//                .sum();
//    }
//
//    public double getTotalSalesByPeriod(String period) {
//        OffsetDateTime now = OffsetDateTime.now();
//        OffsetDateTime startDate;
//
//        switch (period.toLowerCase()) {
//            case "day":
//                startDate = now.truncatedTo(ChronoUnit.DAYS);
//                break;
//            case "week":
//                startDate = now.minusWeeks(1).truncatedTo(ChronoUnit.DAYS);
//                break;
//            case "month":
//                startDate = now.minusMonths(1).truncatedTo(ChronoUnit.DAYS);
//                break;
//            default:
//                throw new IllegalArgumentException("Invalid period. Use 'day', 'week', or 'month'.");
//        }
//        return getTotalSales(startDate, now);
//    }
}
