package org.dti.se.finalproject1backend1.inners.usecases.products;

import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.products.CategoryNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.ProductCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryUseCase {

    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductCustomRepository productCustomRepository;


    public List<CategoryResponse> getCategories(
            Integer page,
            Integer size,
            String search
    ) {
        return productCustomRepository.getCategories(page, size, search);
    }

    public CategoryResponse getCategory(@PathVariable UUID categoryId) {
        Category foundCategory = categoryRepository
                .findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        return productCustomRepository.getCategory(foundCategory.getId());
    }

    public CategoryResponse addCategory(@RequestBody CategoryRequest request) {
        Category category = Category
                .builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        categoryRepository.saveAndFlush(category);

        return getCategory(category.getId());
    }

    public CategoryResponse patchCategory(
            @PathVariable UUID id,
            @RequestBody CategoryRequest request
    ) {
        Category foundCategory = categoryRepository
                .findById(id)
                .orElseThrow(CategoryNotFoundException::new);

        foundCategory.setName(request.getName());
        foundCategory.setDescription(request.getDescription());

        categoryRepository.saveAndFlush(foundCategory);

        return getCategory(foundCategory.getId());
    }

    public void deleteCategory(@PathVariable UUID categoryId) {
        Category foundCategory = categoryRepository
                .findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        categoryRepository.delete(foundCategory);
    }
}
