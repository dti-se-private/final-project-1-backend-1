package org.dti.se.finalproject1backend1.outers.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dti.se.finalproject1backend1.outers.configurations.serdes.HexStringDeserializer;
import org.dti.se.finalproject1backend1.outers.configurations.serdes.HexStringSerializer;
import org.dti.se.finalproject1backend1.outers.configurations.serdes.PointDeserializer;
import org.dti.se.finalproject1backend1.outers.configurations.serdes.PointSerializer;
import org.locationtech.jts.geom.Point;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {
    @Bean
    public SimpleModule pointModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Point.class, new PointDeserializer());
        module.addSerializer(Point.class, new PointSerializer());
        return module;
    }


    @Bean
    public SimpleModule hexModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(byte[].class, new HexStringDeserializer());
        module.addSerializer(byte[].class, new HexStringSerializer());
        return module;
    }

    @Bean
    public JavaTimeModule javaTimeModule() {
        return new JavaTimeModule();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(javaTimeModule());
        objectMapper.registerModule(hexModule());
        objectMapper.registerModule(pointModule());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return objectMapper;
    }
}
