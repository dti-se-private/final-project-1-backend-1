package org.dti.se.finalproject1backend1.inners.models.valueobjects.products;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ProductResponse extends Model {
    private UUID id;
    private CategoryResponse category;
    private String name;
    private String description;
    private Double price;
    private Double quantity;
    private byte[] image;
}
