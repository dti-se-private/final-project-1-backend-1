package org.dti.se.finalproject1backend1.inners.usecases.categories;

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

    public List<Category> getAllCategories(int page, int size, String filters, String search) {
        Pageable pageable = PageRequest.of(page, size);

        return categoryRepository.findCategories(pageable, filters, search);
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
