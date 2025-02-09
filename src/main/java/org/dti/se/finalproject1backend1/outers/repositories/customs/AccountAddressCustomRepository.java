package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountAddressResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AccountAddressCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public List<AccountAddressResponse> getAccountAddresses(
            Account account,
            Integer page,
            Integer size,
            String search
    ) {

        String sql = """
                SELECT *
                FROM (
                    SELECT json_build_object(
                        'id', account_address.id,
                        'name', account_address.name,
                        'address', account_address.address,
                        'is_primary', account_address.is_primary,
                        'location', account_address.location
                    ) as item
                    FROM account_address
                    WHERE account_id = ?
                ) as sq1
                ORDER BY SIMILARITY(sq1.item::text, ?) DESC
                LIMIT ?
                OFFSET ?
                """;

        return oneTemplate
                .query(sql,
                        (rs, rowNum) -> {
                            try {
                                return objectMapper.readValue(rs.getString("item"), new TypeReference<>() {
                                });
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        account.getId(),
                        search,
                        size,
                        page * size
                );
    }
}
