package org.dti.se.finalproject1backend1.outers.repositories.twos;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;


@Repository
public class SessionRepository {

    @Autowired
    RedisTemplate<String, String> stringTemplate;

    public void setByAccessToken(Session session) {
        String key = session.getAccessToken();
        String value = session.toJsonString();
        Duration timeout = Duration.between(
                OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS).toInstant(),
                session.getAccessTokenExpiredAt().toInstant()
        );
        stringTemplate
                .opsForValue()
                .set(key, value, timeout);

    }

    public Session getByAccessToken(String accessToken) {
        String jsonString = stringTemplate
                .opsForValue()
                .get(accessToken);
        try {
            return Jackson2ObjectMapperBuilder.json().build().readValue(jsonString, Session.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteByAccessToken(String accessToken) {
        stringTemplate
                .opsForValue()
                .getOperations()
                .delete(accessToken);
    }

}
