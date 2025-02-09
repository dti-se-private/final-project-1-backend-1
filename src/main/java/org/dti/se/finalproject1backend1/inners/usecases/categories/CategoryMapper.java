package org.dti.se.finalproject1backend1.inners.usecases.categories;

import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    // Convert CategoryDTO to Category
    public Category toEntity(CategoryRequest categoryRequest) {
        if (categoryRequest == null) {
            return null;
        }

        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        return category;
    }

    // Convert Category to CategoryDTO
    public CategoryResponse toDTO(Category category) {
        if (category == null) {
            return null;
        }

        CategoryResponse categoryDTO = new CategoryResponse();
        categoryDTO.setName(category.getName());
        categoryDTO.setDescription(category.getDescription());
        return categoryDTO;
    }

    public CategoryResponse mapCategory(Category category) {
        if (category == null) return null;

        return new CategoryResponse()
                .setId(category.getId())
                .setName(category.getName())
                .setDescription(category.getDescription());
    }
}

