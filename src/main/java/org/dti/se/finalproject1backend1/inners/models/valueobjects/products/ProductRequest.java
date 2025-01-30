package org.dti.se.finalproject1backend1.inners.models.valueobjects.products;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductRequest {
    private UUID categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private byte[] image;
}
