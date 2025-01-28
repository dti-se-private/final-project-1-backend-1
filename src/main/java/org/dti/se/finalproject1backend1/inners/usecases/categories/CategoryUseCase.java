package org.dti.se.finalproject1backend1.inners.usecases.categories;

import jakarta.persistence.EntityNotFoundException;
import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.outers.repositories.ones.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryUseCase {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories(int page, int size, String name, String description) {
        Pageable pageable = PageRequest.of(page, size);

        return categoryRepository.findCategories(pageable, name, description);
    }

    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Category not Found for ID: " + id));
    }

    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category updateCategory(UUID id, Category category) {
        category.setId(id);
        return categoryRepository.save(category);
    }

    public void deleteCategory(UUID id) {
        categoryRepository.deleteById(id);
    }
}
