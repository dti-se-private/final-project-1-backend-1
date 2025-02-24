package org.dti.se.finalproject1backend1.outers.configurations.datastores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.Objects;


@Configuration
public class TwoDatastoreConfiguration {

    @Autowired
    Environment environment;

    @Bean
    public RedisConnectionFactory reactiveRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(Objects.requireNonNull(environment.getProperty("datastore.two.host")));
        config.setPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("datastore.two.port"))));
        config.setPassword(Objects.requireNonNull(environment.getProperty("datastore.two.password")));

        return new LettuceConnectionFactory(config);
    }

}
