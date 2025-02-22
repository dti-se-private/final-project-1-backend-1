package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class StockStatisticCustomRepository {

    @Autowired
    @Qualifier("oneNamedTemplate")
    NamedParameterJdbcTemplate oneNamedTemplate;

    @Autowired
    ObjectMapper objectMapper;

    // --- getProductStockIncrementSum ---

    public List<StatisticSeriesResponse> getProductStockIncrementSum(Account account, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("productIds", productIds);

        return oneNamedTemplate
                .query("""
                                SELECT
                                DATE_TRUNC(:period, stock_ledger.time) as x,
                                SUM(stock_ledger.post_quantity - stock_ledger.pre_quantity) as y
                                FROM stock_ledger
                                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                                INNER JOIN warehouse ON warehouse.id = warehouse_product.warehouse_id
                                INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                                WHERE (stock_ledger.post_quantity - stock_ledger.pre_quantity) > 0
                                AND warehouse_admin.account_id = :accountId::uuid
                                AND warehouse_product.product_id IN (:productIds)
                                GROUP BY x
                                ORDER BY x;
                                """,
                        parameters,
                        this::mapRowToStatisticSeriesResponse
                );
    }

    public List<StatisticSeriesResponse> getProductStockIncrementSum(Account account, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId());

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                SUM(stock_ledger.post_quantity - stock_ledger.pre_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                INNER JOIN warehouse ON warehouse.id = warehouse_product.warehouse_id
                INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                WHERE (stock_ledger.post_quantity - stock_ledger.pre_quantity) > 0
                AND warehouse_admin.account_id = :accountId::uuid
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockIncrementSum(List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                SUM(stock_ledger.post_quantity - stock_ledger.pre_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                WHERE (stock_ledger.post_quantity - stock_ledger.pre_quantity) > 0
                AND warehouse_product.product_id IN (:productIds)
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockIncrementSum(String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                SUM(stock_ledger.post_quantity - stock_ledger.pre_quantity) as y
                FROM stock_ledger
                WHERE (stock_ledger.post_quantity - stock_ledger.pre_quantity) > 0
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }


    // --- getProductStockIncrementAvg ---

    public List<StatisticSeriesResponse> getProductStockIncrementAvg(Account account, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                AVG(stock_ledger.post_quantity - stock_ledger.pre_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                INNER JOIN warehouse ON warehouse.id = warehouse_product.warehouse_id
                INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                WHERE (stock_ledger.post_quantity - stock_ledger.pre_quantity) > 0
                AND warehouse_admin.account_id = :accountId::uuid
                AND warehouse_product.product_id IN (:productIds)
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockIncrementAvg(Account account, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId());

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                AVG(stock_ledger.post_quantity - stock_ledger.pre_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                INNER JOIN warehouse ON warehouse.id = warehouse_product.warehouse_id
                INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                WHERE (stock_ledger.post_quantity - stock_ledger.pre_quantity) > 0
                AND warehouse_admin.account_id = :accountId::uuid
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockIncrementAvg(List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                AVG(stock_ledger.post_quantity - stock_ledger.pre_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                WHERE (stock_ledger.post_quantity - stock_ledger.pre_quantity) > 0
                AND warehouse_product.product_id IN (:productIds)
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockIncrementAvg(String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                AVG(stock_ledger.post_quantity - stock_ledger.pre_quantity) as y
                FROM stock_ledger
                WHERE (stock_ledger.post_quantity - stock_ledger.pre_quantity) > 0
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }


    // --- getProductStockDecrementSum ---

    public List<StatisticSeriesResponse> getProductStockDecrementSum(Account account, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                SUM(stock_ledger.pre_quantity - stock_ledger.post_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                INNER JOIN warehouse ON warehouse.id = warehouse_product.warehouse_id
                INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                WHERE (stock_ledger.pre_quantity - stock_ledger.post_quantity) > 0
                AND warehouse_admin.account_id = :accountId::uuid
                AND warehouse_product.product_id IN (:productIds)
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockDecrementSum(Account account, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId());

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                SUM(stock_ledger.pre_quantity - stock_ledger.post_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                INNER JOIN warehouse ON warehouse.id = warehouse_product.warehouse_id
                INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                WHERE (stock_ledger.pre_quantity - stock_ledger.post_quantity) > 0
                AND warehouse_admin.account_id = :accountId::uuid
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockDecrementSum(List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                SUM(stock_ledger.pre_quantity - stock_ledger.post_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                WHERE (stock_ledger.pre_quantity - stock_ledger.post_quantity) > 0
                AND warehouse_product.product_id IN (:productIds)
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockDecrementSum(String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                SUM(stock_ledger.pre_quantity - stock_ledger.post_quantity) as y
                FROM stock_ledger
                WHERE (stock_ledger.pre_quantity - stock_ledger.post_quantity) > 0
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }


    // --- getProductStockDecrementAvg ---
    public List<StatisticSeriesResponse> getProductStockDecrementAvg(Account account, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                AVG(stock_ledger.pre_quantity - stock_ledger.post_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                INNER JOIN warehouse ON warehouse.id = warehouse_product.warehouse_id
                INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                WHERE (stock_ledger.pre_quantity - stock_ledger.post_quantity) > 0
                AND warehouse_admin.account_id = :accountId::uuid
                AND warehouse_product.product_id IN (:productIds)
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockDecrementAvg(Account account, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId());

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                AVG(stock_ledger.pre_quantity - stock_ledger.post_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                INNER JOIN warehouse ON warehouse.id = warehouse_product.warehouse_id
                INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                WHERE (stock_ledger.pre_quantity - stock_ledger.post_quantity) > 0
                AND warehouse_admin.account_id = :accountId::uuid
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockDecrementAvg(List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                AVG(stock_ledger.pre_quantity - stock_ledger.post_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                WHERE (stock_ledger.pre_quantity - stock_ledger.post_quantity) > 0
                AND warehouse_product.product_id IN (:productIds)
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockDecrementAvg(String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                AVG(stock_ledger.pre_quantity - stock_ledger.post_quantity) as y
                FROM stock_ledger
                WHERE (stock_ledger.pre_quantity - stock_ledger.post_quantity) > 0
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }


    // --- getProductStockCurrentSum ---

    public List<StatisticSeriesResponse> getProductStockCurrentSum(Account account, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                SUM(stock_ledger.post_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                INNER JOIN warehouse ON warehouse.id = warehouse_product.warehouse_id
                INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                WHERE warehouse_admin.account_id = :accountId::uuid
                AND warehouse_product.product_id IN (:productIds)
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockCurrentSum(Account account, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId());

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                SUM(stock_ledger.post_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                INNER JOIN warehouse ON warehouse.id = warehouse_product.warehouse_id
                INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                WHERE warehouse_admin.account_id = :accountId::uuid
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockCurrentSum(List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                SUM(stock_ledger.post_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                WHERE warehouse_product.product_id IN (:productIds)
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockCurrentSum(String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                SUM(stock_ledger.post_quantity) as y
                FROM stock_ledger
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    // --- getProductStockCurrentAvg ---

    public List<StatisticSeriesResponse> getProductStockCurrentAvg(Account account, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                AVG(stock_ledger.post_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                INNER JOIN warehouse ON warehouse.id = warehouse_product.warehouse_id
                INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                WHERE warehouse_admin.account_id = :accountId::uuid
                AND warehouse_product.product_id IN (:productIds)
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockCurrentAvg(Account account, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId());

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                AVG(stock_ledger.post_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                INNER JOIN warehouse ON warehouse.id = warehouse_product.warehouse_id
                INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                WHERE warehouse_admin.account_id = :accountId::uuid
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockCurrentAvg(List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                AVG(stock_ledger.post_quantity) as y
                FROM stock_ledger
                INNER JOIN warehouse_product ON warehouse_product.id = stock_ledger.warehouse_product_id
                WHERE warehouse_product.product_id IN (:productIds)
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductStockCurrentAvg(String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period);

        return oneNamedTemplate.query("""
                SELECT
                DATE_TRUNC(:period, stock_ledger.time) as x,
                AVG(stock_ledger.post_quantity) as y
                FROM stock_ledger
                GROUP BY x
                ORDER BY x;
                """, parameters, this::mapRowToStatisticSeriesResponse);
    }


    // Helper method to map a ResultSet row to a StatisticSeriesResponse
    private StatisticSeriesResponse mapRowToStatisticSeriesResponse(ResultSet rs, int rowNum) throws java.sql.SQLException {
        return StatisticSeriesResponse.builder()
                .x(rs.getObject("x", OffsetDateTime.class))
                .y(rs.getDouble("y"))
                .build();
    }
}
