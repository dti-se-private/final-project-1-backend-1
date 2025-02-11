package org.dti.se.finalproject1backend1.inners.usecases.categories;

import jakarta.persistence.EntityNotFoundException;
import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.products.CategoryNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.CategoryCustomRepository;
import org.dti.se.finalproject1backend1.outers.repositories.ones.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryUseCase {

    @Autowired
    private CategoryCustomRepository categoryRepository;

    public List<CategoryResponse> getAllCategories(int page, int size, List<String> filters, String search) {
        return categoryRepository.getAllCategories(page, size, filters, search);
    }

    public CategoryResponse getCategoryById(UUID id) {
        try {
            return categoryRepository.getById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new CategoryNotFoundException();
        }
    }

    public CategoryResponse addCategory(CategoryRequest request) {
        Category category = new Category()
                .setName(request.getName())
                .setDescription(request.getDescription());

        categoryRepository.create(category);
        return getCategoryById(category.getId());
    }

    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        getCategoryById(id);

        Category category = new Category()
                .setId(id)
                .setName(request.getName())
                .setDescription(request.getDescription());

        categoryRepository.update(category);
        return getCategoryById(id);

    }

    public void deleteCategory(UUID id) {
        getCategoryById(id);
        categoryRepository.delete(id);
    }
}
