package org.dti.se.finalproject1backend1.inners.models.valueobjects.products;

import lombok.Data;
import org.dti.se.finalproject1backend1.inners.models.entities.Category;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductResponse {
    private UUID id;
    private UUID categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private byte[] image;
}
