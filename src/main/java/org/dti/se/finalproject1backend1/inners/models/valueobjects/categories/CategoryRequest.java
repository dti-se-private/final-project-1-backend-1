package org.dti.se.finalproject1backend1.inners.models.valueobjects.categories;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CategoryRequest extends Model {
    private String name;
    private String description;
}
