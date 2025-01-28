package org.dti.se.finalproject1backend1.inners.models.valueobjects.categories;

import lombok.Data;

import java.util.UUID;

@Data
public class CategoryResponse {
    private UUID id;
    private String name;
    private String description;
}
