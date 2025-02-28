package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderStatusCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public void proceedShippingToConfirmedAfterTwoDays() {
        String sql = """
                INSERT INTO order_status (id, order_id, status, time)
                SELECT uuid_generate_v4(), sq1.order_id, 'ORDER_CONFIRMED', now()
                FROM order_status as sq1
                WHERE sq1.order_id in (
                    SELECT sq2.order_id
                    FROM (
                        SELECT *
                        FROM order_status as sq3
                        WHERE sq3.order_id = sq1.order_id
                        ORDER BY sq3.time DESC
                        LIMIT 1
                    ) as sq2
                    WHERE sq2.status = 'SHIPPING'
                )
                AND now() - sq1.time >= interval '2 days'
                """;
        oneTemplate.update(sql);
    }

    public void proceedWaitingForPaymentToCancelledAfterOneHours() {
        String sql = """
                INSERT INTO order_status (id, order_id, status, time)
                SELECT uuid_generate_v4(), sq1.order_id, 'CANCELED', now()
                FROM order_status as sq1
                WHERE sq1.order_id in (
                    SELECT sq2.order_id
                    FROM (
                        SELECT *
                        FROM order_status as sq3
                        WHERE sq3.order_id = sq1.order_id
                        ORDER BY sq3.time DESC
                        LIMIT 1
                    ) as sq2
                    WHERE sq2.status = 'WAITING_FOR_PAYMENT'
                )
                AND now() - sq1.time >= interval '1 hours'
                """;
        oneTemplate.update(sql);
    }
}