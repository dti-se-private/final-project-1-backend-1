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
public class SalesStatisticCustomRepository {

    @Autowired
    @Qualifier("oneNamedTemplate")
    private NamedParameterJdbcTemplate oneNamedTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== INCREMENT (Processing Status) ====================

    // ------ Increment AVG ------
    public List<StatisticSeriesResponse> getProductSalesIncrementAvg(Account account, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                AVG(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'PROCESSING'
            AND o.account_id = :accountId
            AND p.id IN (:productIds)
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesIncrementAvg(Account account, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId());

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                AVG(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'PROCESSING'
            AND o.account_id = :accountId
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesIncrementAvg(List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                AVG(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'PROCESSING'
            AND p.id IN (:productIds)
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesIncrementAvg(String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period);

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                AVG(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'PROCESSING'
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    // ------ Increment SUM ------
    public List<StatisticSeriesResponse> getProductSalesIncrementSum(Account account, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                SUM(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'PROCESSING'
            AND o.account_id = :accountId
            AND p.id IN (:productIds)
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesIncrementSum(Account account, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId());

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                SUM(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'PROCESSING'
            AND o.account_id = :accountId
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesIncrementSum(List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                SUM(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'PROCESSING'
            AND p.id IN (:productIds)
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesIncrementSum(String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period);

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                SUM(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'PROCESSING'
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }
    // ==================== DECREMENT (Cancelled after Processing) ====================

    // ------ Decrement Sum ------
    public List<StatisticSeriesResponse> getProductSalesDecrementSum(Account account, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                SUM(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'CANCELED'
            AND EXISTS (
                SELECT 1 FROM order_status os
                WHERE os.order_id = latest_status.order_id
                AND os.status = 'PROCESSING'
                AND os.time < latest_status.time
            )
            AND o.account_id = :accountId
            AND p.id IN (:productIds)
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesDecrementSum(Account account, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId());

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                SUM(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'CANCELED'
            AND EXISTS (
                SELECT 1 FROM order_status os
                WHERE os.order_id = latest_status.order_id
                AND os.status = 'PROCESSING'
                AND os.time < latest_status.time
            )
            AND o.account_id = :accountId
            AND p.id IN (:productIds)
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesDecrementSum(List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                SUM(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'CANCELED'
            AND EXISTS (
                SELECT 1 FROM order_status os
                WHERE os.order_id = latest_status.order_id
                AND os.status = 'PROCESSING'
                AND os.time < latest_status.time
            )
            AND o.account_id = :accountId
            AND p.id IN (:productIds)
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesDecrementSum(String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period);

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                SUM(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'CANCELED'
            AND EXISTS (
                SELECT 1 FROM order_status os
                WHERE os.order_id = latest_status.order_id
                AND os.status = 'PROCESSING'
                AND os.time < latest_status.time
            )
            AND o.account_id = :accountId
            AND p.id IN (:productIds)
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }
    // ------ Decrement Avg ------
    public List<StatisticSeriesResponse> getProductSalesDecrementAvg(Account account, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                AVG(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'CANCELED'
            AND EXISTS (
                SELECT 1 FROM order_status os
                WHERE os.order_id = latest_status.order_id
                AND os.status = 'PROCESSING'
                AND os.time < latest_status.time
            )
            AND o.account_id = :accountId
            AND p.id IN (:productIds)
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesDecrementAvg(Account account, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId());

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                AVG(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'CANCELED'
            AND EXISTS (
                SELECT 1 FROM order_status os
                WHERE os.order_id = latest_status.order_id
                AND os.status = 'PROCESSING'
                AND os.time < latest_status.time
            )
            AND o.account_id = :accountId
            AND p.id IN (:productIds)
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesDecrementAvg(List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                AVG(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'CANCELED'
            AND EXISTS (
                SELECT 1 FROM order_status os
                WHERE os.order_id = latest_status.order_id
                AND os.status = 'PROCESSING'
                AND os.time < latest_status.time
            )
            AND o.account_id = :accountId
            AND p.id IN (:productIds)
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesDecrementAvg(String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period);

        return oneNamedTemplate.query("""
            SELECT
                DATE_TRUNC(:period, latest_status.time) AS x,
                AVG(oi.quantity * p.price) AS y
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN "order" o ON oi.order_id = o.id
            JOIN (
                SELECT DISTINCT ON (order_id) 
                    order_id, 
                    status, 
                    time
                FROM order_status 
                ORDER BY order_id, time DESC
            ) latest_status ON o.id = latest_status.order_id
            WHERE latest_status.status = 'CANCELED'
            AND EXISTS (
                SELECT 1 FROM order_status os
                WHERE os.order_id = latest_status.order_id
                AND os.status = 'PROCESSING'
                AND os.time < latest_status.time
            )
            AND o.account_id = :accountId
            AND p.id IN (:productIds)
            GROUP BY x
            ORDER BY x
            """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    // --- getProductSalesCurrentSum ---
    public List<StatisticSeriesResponse> getProductSalesCurrentSum(Account account, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
        SELECT
            DATE_TRUNC(:period, latest_status.time) AS x,
            SUM(oi.quantity * p.price) AS y
        FROM order_item oi
        JOIN product p ON oi.product_id = p.id
        JOIN "order" o ON oi.order_id = o.id
        JOIN (
            SELECT DISTINCT ON (order_id) 
                order_id, 
                status, 
                time
            FROM order_status 
            ORDER BY order_id, time DESC
        ) latest_status ON o.id = latest_status.order_id
        WHERE latest_status.status = 'PROCESSING'
        AND o.account_id = :accountId
        AND p.id IN (:productIds)
        GROUP BY x
        ORDER BY x
        """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesCurrentSum(Account account, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId());

        return oneNamedTemplate.query("""
        SELECT
            DATE_TRUNC(:period, latest_status.time) AS x,
            SUM(oi.quantity * p.price) AS y
        FROM order_item oi
        JOIN product p ON oi.product_id = p.id
        JOIN "order" o ON oi.order_id = o.id
        JOIN (
            SELECT DISTINCT ON (order_id) 
                order_id, 
                status, 
                time
            FROM order_status 
            ORDER BY order_id, time DESC
        ) latest_status ON o.id = latest_status.order_id
        WHERE latest_status.status = 'PROCESSING'
        AND o.account_id = :accountId
        GROUP BY x
        ORDER BY x
        """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesCurrentSum(List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
        SELECT
            DATE_TRUNC(:period, latest_status.time) AS x,
            SUM(oi.quantity * p.price) AS y
        FROM order_item oi
        JOIN product p ON oi.product_id = p.id
        JOIN "order" o ON oi.order_id = o.id
        JOIN (
            SELECT DISTINCT ON (order_id) 
                order_id, 
                status, 
                time
            FROM order_status 
            ORDER BY order_id, time DESC
        ) latest_status ON o.id = latest_status.order_id
        WHERE latest_status.status = 'PROCESSING'
        AND p.id IN (:productIds)
        GROUP BY x
        ORDER BY x
        """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesCurrentSum(String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period);

        return oneNamedTemplate.query("""
        SELECT
            DATE_TRUNC(:period, latest_status.time) AS x,
            SUM(oi.quantity * p.price) AS y
        FROM order_item oi
        JOIN product p ON oi.product_id = p.id
        JOIN "order" o ON oi.order_id = o.id
        JOIN (
            SELECT DISTINCT ON (order_id) 
                order_id, 
                status, 
                time
            FROM order_status 
            ORDER BY order_id, time DESC
        ) latest_status ON o.id = latest_status.order_id
        WHERE latest_status.status = 'PROCESSING'
        GROUP BY x
        ORDER BY x
        """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    // --- getProductSalesCurrentAvg ---
    public List<StatisticSeriesResponse> getProductSalesCurrentAvg(Account account, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
        SELECT
            DATE_TRUNC(:period, latest_status.time) AS x,
            AVG(oi.quantity * p.price) AS y
        FROM order_item oi
        JOIN product p ON oi.product_id = p.id
        JOIN "order" o ON oi.order_id = o.id
        JOIN (
            SELECT DISTINCT ON (order_id) 
                order_id, 
                status, 
                time
            FROM order_status 
            ORDER BY order_id, time DESC
        ) latest_status ON o.id = latest_status.order_id
        WHERE latest_status.status = 'PROCESSING'
        AND o.account_id = :accountId
        AND p.id IN (:productIds)
        GROUP BY x
        ORDER BY x
        """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesCurrentAvg(Account account, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId());

        return oneNamedTemplate.query("""
        SELECT
            DATE_TRUNC(:period, latest_status.time) AS x,
            AVG(oi.quantity * p.price) AS y
        FROM order_item oi
        JOIN product p ON oi.product_id = p.id
        JOIN "order" o ON oi.order_id = o.id
        JOIN (
            SELECT DISTINCT ON (order_id) 
                order_id, 
                status, 
                time
            FROM order_status 
            ORDER BY order_id, time DESC
        ) latest_status ON o.id = latest_status.order_id
        WHERE latest_status.status = 'PROCESSING'
        AND o.account_id = :accountId
        GROUP BY x
        ORDER BY x
        """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesCurrentAvg(List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("productIds", productIds);

        return oneNamedTemplate.query("""
        SELECT
            DATE_TRUNC(:period, latest_status.time) AS x,
            AVG(oi.quantity * p.price) AS y
        FROM order_item oi
        JOIN product p ON oi.product_id = p.id
        JOIN "order" o ON oi.order_id = o.id
        JOIN (
            SELECT DISTINCT ON (order_id) 
                order_id, 
                status, 
                time
            FROM order_status 
            ORDER BY order_id, time DESC
        ) latest_status ON o.id = latest_status.order_id
        WHERE latest_status.status = 'PROCESSING'
        AND p.id IN (:productIds)
        GROUP BY x
        ORDER BY x
        """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesCurrentAvg(String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period);

        return oneNamedTemplate.query("""
        SELECT
            DATE_TRUNC(:period, latest_status.time) AS x,
            AVG(oi.quantity * p.price) AS y
        FROM order_item oi
        JOIN product p ON oi.product_id = p.id
        JOIN "order" o ON oi.order_id = o.id
        JOIN (
            SELECT DISTINCT ON (order_id) 
                order_id, 
                status, 
                time
            FROM order_status 
            ORDER BY order_id, time DESC
        ) latest_status ON o.id = latest_status.order_id
        WHERE latest_status.status = 'PROCESSING'
        GROUP BY x
        ORDER BY x
        """, parameters, this::mapRowToStatisticSeriesResponse);
    }

    private StatisticSeriesResponse mapRowToStatisticSeriesResponse(ResultSet rs, int rowNum) throws java.sql.SQLException {
        return StatisticSeriesResponse.builder()
                .x(rs.getObject("x", OffsetDateTime.class))
                .y(rs.getDouble("y"))
                .build();
    }
}