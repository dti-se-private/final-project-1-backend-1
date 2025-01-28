package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Category;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
import org.dti.se.finalproject1backend1.inners.usecases.categories.CategoryMapper;
import org.dti.se.finalproject1backend1.inners.usecases.categories.CategoryUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product-categories")
public class CategoryRest {

    @Autowired
    private CategoryUseCase categoryService;

    @Autowired
    private CategoryMapper categoryMapper;
    @GetMapping
    public List<CategoryResponse> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description
    ) {
        return categoryService.getAllCategories(page, size, name, description)
                .stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategoryById(@PathVariable UUID id) {
        Category category = categoryService.getCategoryById(id);
        return convertToCategoryDTO(category);
    }

    @PostMapping
//    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public Category addCategory(@RequestBody CategoryRequest categoryRequest) {
        Category category = categoryMapper.toEntity(categoryRequest);

        Category savedCategory =categoryService.addCategory(category);
        return savedCategory;
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasAuthority('SUPER_ADMIN)")
    public CategoryResponse updateCategory(@PathVariable UUID id, @RequestBody CategoryRequest categoryRequest) {
        Category category = categoryMapper.toEntity(categoryRequest);
        Category updatedCategory = categoryService.updateCategory(id, category);
        return convertToCategoryDTO(updatedCategory);
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<String> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Category deleted Successfully.");
    }


    // Entities convert into DTO
    private CategoryResponse convertToCategoryDTO(Category category) {
        CategoryResponse dto = new CategoryResponse();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }

}
