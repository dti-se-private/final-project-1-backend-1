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


    public List<StatisticSeriesResponse> getProductSalesSum(Account account, List<UUID> categoryIds, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("categoryIds", categoryIds)
                .addValue("productIds", productIds);

        String sql;
        if (categoryIds.isEmpty() && productIds.isEmpty()) {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        SUM(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                        AND order_status.order_id in (
                            SELECT DISTINCT "order".id
                            FROM "order"
                            INNER JOIN warehouse ON warehouse.id = "order".origin_warehouse_id
                            INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                            WHERE "order".id = order_status.order_id
                            AND warehouse_admin.account_id = :accountId
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    GROUP BY x
                    ORDER BY x
                    """;
        } else if (categoryIds.isEmpty()) {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        SUM(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                        AND order_status.order_id in (
                            SELECT DISTINCT "order".id
                            FROM "order"
                            INNER JOIN warehouse ON warehouse.id = "order".origin_warehouse_id
                            INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                            WHERE "order".id = order_status.order_id
                            AND warehouse_admin.account_id = :accountId
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    AND product.id IN (:productIds)
                    GROUP BY x
                    ORDER BY x
                    """;
        } else if (productIds.isEmpty()) {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        SUM(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                        AND order_status.order_id in (
                            SELECT DISTINCT "order".id
                            FROM "order"
                            INNER JOIN warehouse ON warehouse.id = "order".origin_warehouse_id
                            INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                            WHERE "order".id = order_status.order_id
                            AND warehouse_admin.account_id = :accountId
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    WHERE category.id IN (:categoryIds)
                    GROUP BY x
                    ORDER BY x
                    """;
        } else {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        SUM(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                        AND order_status.order_id in (
                            SELECT DISTINCT "order".id
                            FROM "order"
                            INNER JOIN warehouse ON warehouse.id = "order".origin_warehouse_id
                            INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                            WHERE "order".id = order_status.order_id
                            AND warehouse_admin.account_id = :accountId
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    WHERE category.id IN (:categoryIds)
                    AND product.id IN (:productIds)
                    GROUP BY x
                    ORDER BY x
                    """;
        }

        return oneNamedTemplate.query(sql, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesSum(List<UUID> categoryIds, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("categoryIds", categoryIds)
                .addValue("productIds", productIds);

        String sql;
        if (categoryIds.isEmpty() && productIds.isEmpty()) {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        SUM(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    GROUP BY x
                    ORDER BY x
                    """;
        } else if (categoryIds.isEmpty()) {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        SUM(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    AND product.id IN (:productIds)
                    GROUP BY x
                    ORDER BY x
                    """;
        } else if (productIds.isEmpty()) {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        SUM(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    WHERE category.id IN (:categoryIds)
                    GROUP BY x
                    ORDER BY x
                    """;
        } else {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        SUM(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    WHERE category.id IN (:categoryIds)
                    AND product.id IN (:productIds)
                    GROUP BY x
                    ORDER BY x
                    """;
        }

        return oneNamedTemplate.query(sql, parameters, this::mapRowToStatisticSeriesResponse);
    }


    public List<StatisticSeriesResponse> getProductSalesAvg(Account account, List<UUID> categoryIds, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("accountId", account.getId())
                .addValue("categoryIds", categoryIds)
                .addValue("productIds", productIds);

        String sql;
        if (categoryIds.isEmpty() && productIds.isEmpty()) {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        AVG(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                        AND order_status.order_id in (
                            SELECT DISTINCT "order".id
                            FROM "order"
                            INNER JOIN warehouse ON warehouse.id = "order".origin_warehouse_id
                            INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                            WHERE "order".id = order_status.order_id
                            AND warehouse_admin.account_id = :accountId
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    GROUP BY x
                    ORDER BY x
                    """;
        } else if (categoryIds.isEmpty()) {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        AVG(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                        AND order_status.order_id in (
                            SELECT DISTINCT "order".id
                            FROM "order"
                            INNER JOIN warehouse ON warehouse.id = "order".origin_warehouse_id
                            INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                            WHERE "order".id = order_status.order_id
                            AND warehouse_admin.account_id = :accountId
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    AND product.id IN (:productIds)
                    GROUP BY x
                    ORDER BY x
                    """;
        } else if (productIds.isEmpty()) {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        AVG(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                        AND order_status.order_id in (
                            SELECT DISTINCT "order".id
                            FROM "order"
                            INNER JOIN warehouse ON warehouse.id = "order".origin_warehouse_id
                            INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                            WHERE "order".id = order_status.order_id
                            AND warehouse_admin.account_id = :accountId
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    WHERE category.id IN (:categoryIds)
                    GROUP BY x
                    ORDER BY x
                    """;
        } else {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        AVG(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                        AND order_status.order_id in (
                            SELECT DISTINCT "order".id
                            FROM "order"
                            INNER JOIN warehouse ON warehouse.id = "order".origin_warehouse_id
                            INNER JOIN warehouse_admin ON warehouse_admin.warehouse_id = warehouse.id
                            WHERE "order".id = order_status.order_id
                            AND warehouse_admin.account_id = :accountId
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    WHERE category.id IN (:categoryIds)
                    AND product.id IN (:productIds)
                    GROUP BY x
                    ORDER BY x
                    """;
        }

        return oneNamedTemplate.query(sql, parameters, this::mapRowToStatisticSeriesResponse);
    }

    public List<StatisticSeriesResponse> getProductSalesAvg(List<UUID> categoryIds, List<UUID> productIds, String period) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("period", period)
                .addValue("categoryIds", categoryIds)
                .addValue("productIds", productIds);

        String sql;
        if (categoryIds.isEmpty() && productIds.isEmpty()) {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        AVG(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    GROUP BY x
                    ORDER BY x
                    """;
        } else if (categoryIds.isEmpty()) {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        AVG(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    AND product.id IN (:productIds)
                    GROUP BY x
                    ORDER BY x
                    """;
        } else if (productIds.isEmpty()) {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        AVG(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    WHERE category.id IN (:categoryIds)
                    GROUP BY x
                    ORDER BY x
                    """;
        } else {
            sql = """
                    SELECT
                        DATE_TRUNC(:period, sq1.time) as x,
                        AVG(order_item.quantity * product.price) as y
                    FROM (
                        SELECT *
                        FROM order_status
                        WHERE order_status.id in (
                            SELECT sq2.id
                                FROM (
                                    SELECT *
                                    FROM order_status sq2_os
                                    WHERE sq2_os.order_id = order_status.order_id
                                    ORDER BY sq2_os.time DESC
                                    LIMIT 1
                                ) as sq2
                                WHERE sq2.status = 'ORDER_CONFIRMED'
                        )
                    ) as sq1
                    INNER JOIN order_item on order_item.order_id = sq1.order_id
                    INNER JOIN product ON product.id = order_item.product_id
                    INNER JOIN category ON category.id = product.category_id
                    WHERE category.id IN (:categoryIds)
                    AND product.id IN (:productIds)
                    GROUP BY x
                    ORDER BY x
                    """;
        }

        return oneNamedTemplate.query(sql, parameters, this::mapRowToStatisticSeriesResponse);
    }


    private StatisticSeriesResponse mapRowToStatisticSeriesResponse(ResultSet rs, int rowNum) throws java.sql.SQLException {
        return StatisticSeriesResponse.builder()
                .x(rs.getObject("x", OffsetDateTime.class))
                .y(rs.getDouble("y"))
                .build();
    }
}