package org.dti.se.finalproject1backend1.outers.repositories.customs;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class StatisticCustomRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<StatisticSeriesResponse> retrieveTransactionAmountAverage(Account account, String period) {
        return jdbcTemplate
                .query("""
                                SELECT
                                DATE_TRUNC(?, t.time) as x,
                                AVG(et.price) as y
                                FROM transaction t
                                INNER JOIN event e ON e.id = t.event_id
                                INNER JOIN event_ticket et ON et.event_id = e.id
                                WHERE e.account_id = ?::uuid
                                GROUP BY x
                                ORDER BY x
                                """,
                        (rs, rowNum) -> StatisticSeriesResponse
                                .builder()
                                .x(rs.getObject("x", OffsetDateTime.class))
                                .y(rs.getDouble("y"))
                                .build(),
                        period,
                        account.getId()
                );
    }

    public List<StatisticSeriesResponse> retrieveTransactionAmountSum(Account account, String period) {
        return jdbcTemplate
                .query("""
                                SELECT
                                DATE_TRUNC(?, t.time) as x,
                                SUM(et.price) as y
                                FROM transaction t
                                INNER JOIN event e ON e.id = t.event_id
                                INNER JOIN event_ticket et ON et.event_id = e.id
                                WHERE e.account_id = ?::uuid
                                GROUP BY x
                                ORDER BY x
                                """,
                        (rs, rowNum) -> StatisticSeriesResponse
                                .builder()
                                .x(rs.getObject("x", OffsetDateTime.class))
                                .y(rs.getDouble("y"))
                                .build(),
                        period,
                        account.getId()
                );
    }

    public List<StatisticSeriesResponse> retrieveParticipantCountAverage(Account account, String period) {
        return jdbcTemplate
                .query("""
                                SELECT
                                DATE_TRUNC(?, t.time) as x,
                                AVG((select count(*) from transaction t2 where t2.id = t.id)) as y
                                FROM transaction t
                                INNER JOIN event e ON e.id = t.event_id
                                WHERE e.account_id = ?::uuid
                                GROUP BY x
                                ORDER BY x;
                                """,
                        (rs, rowNum) -> StatisticSeriesResponse
                                .builder()
                                .x(rs.getObject("x", OffsetDateTime.class))
                                .y(rs.getDouble("y"))
                                .build(),
                        period,
                        account.getId()
                );
    }

    public List<StatisticSeriesResponse> retrieveParticipantCountSum(Account account, String period) {
        return jdbcTemplate
                .query("""
                                SELECT
                                DATE_TRUNC(?, t.time) as x,
                                SUM((select count(*) from transaction t2 where t2.id = t.id)) as y
                                FROM transaction t
                                INNER JOIN event e ON e.id = t.event_id
                                WHERE e.account_id = ?::uuid
                                GROUP BY x
                                ORDER BY x;
                                """,
                        (rs, rowNum) -> StatisticSeriesResponse
                                .builder()
                                .x(rs.getObject("x", OffsetDateTime.class))
                                .y(rs.getDouble("y"))
                                .build(),
                        period,
                        account.getId()
                );
    }
}