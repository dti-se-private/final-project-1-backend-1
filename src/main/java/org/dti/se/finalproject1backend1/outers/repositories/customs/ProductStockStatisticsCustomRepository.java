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
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ProductStockStatisticsCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    private JdbcTemplate oneTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public List<StatisticSeriesResponse> findStockHistoryAggregates(
            List<UUID> warehouseIds,
            List<UUID> productIds,
            String operation,
            String period
    ) {
        String operationCondition = operation.equalsIgnoreCase("increment")
                ? "quantity_change > 0"
                : "quantity_change < 0";

        String sql = String.format("""
                        SELECT json_build_object(
                            'x', date_trunc('%s', created_at),
                            'y', SUM(ABS(quantity_change))
                        ) AS statistic_series
                        FROM stock_history
                        WHERE warehouse_id IN (%s)
                          AND product_id IN (%s)
                          AND %s
                        GROUP BY date_trunc('%s', created_at)
                        ORDER BY date_trunc('%s', created_at)
                        """,
                period.toLowerCase(),
                warehouseIds.isEmpty()
                        ? "NULL"
                        : warehouseIds.stream()
                        .map(u -> String.format("'%s'", u))
                        .collect(Collectors.joining(",")),
                productIds.isEmpty()
                        ? "NULL"
                        : productIds.stream()
                        .map(u -> String.format("'%s'", u))
                        .collect(Collectors.joining(",")),
                operationCondition,
                period.toLowerCase(),
                period.toLowerCase());

        return oneTemplate.query(sql, (rs, rowNum) -> {
            try {
                return objectMapper.readValue(
                        rs.getString("statistic_series"),
                        new TypeReference<StatisticSeriesResponse>() {
                        }
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse statistic series", e);
            }
        });
    }

    public Double findCurrentStockSum(List<UUID> warehouseIds, List<UUID> productIds) {
        String sql = String.format("""
                        SELECT json_build_object(
                            'x', NOW(),
                            'y', COALESCE(SUM(quantity), 0)
                        ) AS statistic_series
                        FROM warehouse_product
                        WHERE warehouse_id IN (%s)
                          AND product_id IN (%s)
                        """,
                warehouseIds.isEmpty()
                        ? "NULL"
                        : warehouseIds.stream()
                        .map(u -> String.format("'%s'", u))
                        .collect(Collectors.joining(",")),
                productIds.isEmpty()
                        ? "NULL"
                        : productIds.stream()
                        .map(u -> String.format("'%s'", u))
                        .collect(Collectors.joining(",")));

        return oneTemplate.queryForObject(sql, Double.class);
    }
}   
