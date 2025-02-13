package org.dti.se.finalproject1backend1.outers.repositories.customs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CategoryCustomRepository {
    @Autowired
    @Qualifier("oneTemplate")
    JdbcTemplate oneTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public List<CategoryResponse> getAllCategories(
            Integer page,
            Integer size,
            List<String> filters,
            String search
    ) {
        String order = filters.stream()
                .map(filter -> String.format("SIMILARITY(%s::text, '%s')", filter, search))
                .collect(Collectors.joining("+"));

        if (order.isEmpty()) {
            order = "id";
        }

        String sql = String.format("""
            SELECT json_build_object(
                'id', id,
                'name', name,
                'description', description
            ) as item
            FROM category
            ORDER BY %s
            LIMIT ?
            OFFSET ?
            """, order);

        return oneTemplate.query(
                sql,
                (rs, rowNum) -> {
                    try {
                        return objectMapper.readValue(rs.getString("item"), new TypeReference<CategoryResponse>() {});
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                size,
                page * size
        );
    }

    public CategoryResponse getById(UUID id) {
        String sql = """
            SELECT json_build_object(
                'id', id,
                'name', name,
                'description', description
            ) as item
            FROM category
            WHERE id = ?
            """;

        return oneTemplate.queryForObject(
                sql,
                (rs, rowNum) -> {
                    try {
                        return objectMapper.readValue(rs.getString("item"), new TypeReference<CategoryResponse>() {});
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                id
        );
    }

    public void create(Category category) {
        oneTemplate.update("""
            INSERT INTO category (id, name, description)
            VALUES (?, ?, ?)
            """,
                UUID.randomUUID(),
                category.getName(),
                category.getDescription()
        );
    }

    public void update(Category category) {
        oneTemplate.update("""
            UPDATE category
            SET name = ?, description = ?
            WHERE id = ?
            """,
                category.getName(),
                category.getDescription(),
                category.getId()
        );
    }

    public void delete(UUID id) {
        oneTemplate.update("""
            DELETE FROM category
            WHERE id = ?
            """,
                id
        );
    }
}
