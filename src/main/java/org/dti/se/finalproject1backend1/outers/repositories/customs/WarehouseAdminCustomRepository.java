package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.warehouse.admin.WarehouseAdminResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class WarehouseAdminCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public List<WarehouseAdminResponse> getAllWarehouseAdmins(
            Integer page,
            Integer size,
            List<String> filters,
            String search
    ) {
        String order = filters
                .stream()
                .map(filter -> String.format("SIMILARITY(%s::text, '%s')", filter, search))
                .collect(Collectors.joining("+"));

        if (order.isEmpty()) {
            order = "warehouse_admin.id";
        }

        String sql = String.format("""
                SELECT json_build_object(
                        'id', warehouse_admin.id,
                        'warehouseId', warehouse_admin.warehouse_id,
                        'accountId', warehouse_admin.account_id
                    ) as warehouse_admin
                FROM warehouse_admin
                ORDER BY %s
                LIMIT ?
                OFFSET ?
                """, order);

        return oneTemplate
                .query(sql,
                        (rs, rowNum) -> {
                            try {
                                return objectMapper.readValue(rs.getString("warehouse_admin"), new TypeReference<>() {});
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        size,
                        page * size
                );
    }
}
