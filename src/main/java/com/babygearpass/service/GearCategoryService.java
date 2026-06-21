package com.babygearpass.service;

import com.babygearpass.dto.category.CategoryDTO;
import com.babygearpass.dto.category.CategoryRequest;
import com.babygearpass.entity.GearCategory;
import com.babygearpass.repository.GearCategoryRepository;
import com.babygearpass.repository.GearItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GearCategoryService {

    private final GearCategoryRepository gearCategoryRepository;
    private final GearItemRepository gearItemRepository;

    public Page<CategoryDTO> getAllCategories(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return gearCategoryRepository.findByNameContainingIgnoreCase(keyword, pageable).map(this::toDTO);
        }
        return gearCategoryRepository.findAll(pageable).map(this::toDTO);
    }

    public CategoryDTO getCategoryById(Long id) {
        GearCategory category = gearCategoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在"));
        return toDTO(category);
    }

    public CategoryDTO createCategory(CategoryRequest request) {
        if (gearCategoryRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分类名称已存在");
        }

        GearCategory category = new GearCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());

        gearCategoryRepository.save(category);
        return toDTO(category);
    }

    public CategoryDTO updateCategory(Long id, CategoryRequest request) {
        GearCategory category = gearCategoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在"));

        if (!category.getName().equals(request.getName())) {
            if (gearCategoryRepository.findByName(request.getName()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分类名称已存在");
            }
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());

        gearCategoryRepository.save(category);
        return toDTO(category);
    }

    public void deleteCategory(Long id) {
        GearCategory category = gearCategoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在"));

        long itemCount = gearItemRepository.findByCategoryId(id, Pageable.unpaged()).getTotalElements();
        if (itemCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该分类下存在用品，无法删除");
        }

        gearCategoryRepository.delete(category);
    }

    private CategoryDTO toDTO(GearCategory category) {
        long gearCount = gearItemRepository.findByCategoryId(category.getId(), Pageable.unpaged()).getTotalElements();
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getSortOrder(),
                category.getCreatedAt(),
                gearCount
        );
    }
}
