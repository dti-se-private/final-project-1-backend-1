package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductSalesStatisticsCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public List<StatisticSeriesResponse> getProductSalesStatistics(
            List<String> warehouseIds,
            List<String> categoryIds,
            List<String> productIds,
            String aggregation,
            String period
    ) {
        // 1. Validate period and aggregation
        if (!List.of("day", "week", "month").contains(period.toLowerCase())) {
            throw new IllegalArgumentException("Invalid period: " + period);
        }
        if (!List.of("sum", "avg", "count").contains(aggregation.toLowerCase())) {
            throw new IllegalArgumentException("Invalid aggregation: " + aggregation);
        }

        // 2. Build SQL query directly (no helper methods)
        String sql = String.format("""
            SELECT json_build_object(
                'x', DATE_TRUNC('%s', sales.date),
                'y', %s(sales.quantity)
            ) AS statistic_series
            FROM sales
            WHERE (?::text[] IS NULL OR sales.warehouse_id::text = ANY(?))
              AND (?::text[] IS NULL OR sales.category_id::text = ANY(?))
              AND (?::text[] IS NULL OR sales.product_id::text = ANY(?))
            GROUP BY DATE_TRUNC('%s', sales.date)
            ORDER BY DATE_TRUNC('%s', sales.date)
            """, period, aggregation, period, period);

        // 3. Prepare parameters inline (no helper methods)
        Object[] params = new Object[]{
                warehouseIds != null && !warehouseIds.isEmpty() ? warehouseIds.toArray(new String[0]) : null,
                warehouseIds != null && !warehouseIds.isEmpty() ? warehouseIds.toArray(new String[0]) : null,
                categoryIds != null && !categoryIds.isEmpty() ? categoryIds.toArray(new String[0]) : null,
                categoryIds != null && !categoryIds.isEmpty() ? categoryIds.toArray(new String[0]) : null,
                productIds != null && !productIds.isEmpty() ? productIds.toArray(new String[0]) : null,
                productIds != null && !productIds.isEmpty() ? productIds.toArray(new String[0]) : null
        };

        // 4. Execute query and deserialize
        return oneTemplate.query(
                sql,
                (rs, rowNum) -> {
                    try {
                        return objectMapper.readValue(
                                rs.getString("statistic_series"),
                                new TypeReference<StatisticSeriesResponse>() {}
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                params
        );
    }
}