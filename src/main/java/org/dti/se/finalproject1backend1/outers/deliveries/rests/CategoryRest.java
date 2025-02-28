package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.categories.CategoryResponse;
import org.dti.se.finalproject1backend1.inners.usecases.products.CategoryUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.products.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
public class CategoryRest {

    @Autowired
    CategoryUseCase categoryUseCase;

    @GetMapping
    public ResponseEntity<ResponseBody<List<CategoryResponse>>> getCategories(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(defaultValue = "") String search
    ) {
        try {
            List<CategoryResponse> categories = categoryUseCase.getCategories(page, size, search);
            return ResponseBody
                    .<List<CategoryResponse>>builder()
                    .message("Categories found.")
                    .data(categories)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (Exception e) {
            return ResponseBody
                    .<List<CategoryResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ResponseBody<CategoryResponse>> getCategory(@PathVariable UUID categoryId) {
        try {
            CategoryResponse category = categoryUseCase.getCategory(categoryId);
            return ResponseBody
                    .<CategoryResponse>builder()
                    .message("Category found.")
                    .data(category)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (CategoryNotFoundException e) {
            return ResponseBody
                    .<CategoryResponse>builder()
                    .message("Category not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<CategoryResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<CategoryResponse>> addCategory(@RequestBody CategoryRequest request) {
        try {
            CategoryResponse category = categoryUseCase.addCategory(request);
            return ResponseBody
                    .<CategoryResponse>builder()
                    .message("Category added.")
                    .data(category)
                    .build()
                    .toEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseBody
                    .<CategoryResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<CategoryResponse>> patchCategory(
            @PathVariable UUID categoryId,
            @RequestBody CategoryRequest request
    ) {
        try {
            CategoryResponse category = categoryUseCase.patchCategory(categoryId, request);
            return ResponseBody
                    .<CategoryResponse>builder()
                    .message("Category patched.")
                    .data(category)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (CategoryNotFoundException e) {
            return ResponseBody
                    .<CategoryResponse>builder()
                    .message("Category not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<CategoryResponse>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseBody<Void>> deleteCategory(@PathVariable UUID categoryId) {
        try {
            categoryUseCase.deleteCategory(categoryId);
            return ResponseBody
                    .<Void>builder()
                    .message("Category deleted.")
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (CategoryNotFoundException e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Category not found.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseBody
                    .<Void>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
