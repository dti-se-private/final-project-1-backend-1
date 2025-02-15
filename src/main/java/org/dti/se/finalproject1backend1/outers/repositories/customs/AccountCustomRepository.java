package org.dti.se.finalproject1backend1.outers.repositories.customs;

import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AccountCustomRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<AccountResponse> getAdmins() {
        String sql = """
                SELECT id, name, email, password, phone, image, is_verified
                FROM account
                WHERE id IN (
                    SELECT account_id
                    FROM account_permission
                    WHERE permission IN ('SUPER_ADMIN', 'WAREHOUSE_ADMIN')
                )
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> AccountResponse.builder()
                .id(UUID.fromString(rs.getString("id")))
                .name(rs.getString("name"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .phone(rs.getString("phone"))
                .image(rs.getBytes("image"))
                .isVerified(rs.getBoolean("is_verified"))
                .build());
    }
}
