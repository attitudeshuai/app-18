package com.babygearpass.controller;

import com.babygearpass.dto.category.CategoryDTO;
import com.babygearpass.dto.category.CategoryRequest;
import com.babygearpass.dto.common.ApiResponse;
import com.babygearpass.dto.common.PageResponse;
import com.babygearpass.service.GearCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gearcategories")
@RequiredArgsConstructor
public class GearCategoryController {

    private final GearCategoryService gearCategoryService;

    @GetMapping
    public ApiResponse<PageResponse<CategoryDTO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<CategoryDTO> result = gearCategoryService.getAllCategories(keyword, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @PostMapping
    public ApiResponse<CategoryDTO> create(@RequestBody @Valid CategoryRequest request) {
        return ApiResponse.success(gearCategoryService.createCategory(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryDTO> get(@PathVariable Long id) {
        return ApiResponse.success(gearCategoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryDTO> update(@PathVariable Long id, @RequestBody @Valid CategoryRequest request) {
        return ApiResponse.success(gearCategoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        gearCategoryService.deleteCategory(id);
        return ApiResponse.success(null);
    }

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
