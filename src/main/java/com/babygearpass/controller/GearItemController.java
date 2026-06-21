package com.babygearpass.controller;

import com.babygearpass.dto.common.ApiResponse;
import com.babygearpass.dto.common.PageResponse;
import com.babygearpass.dto.gear.GearItemDTO;
import com.babygearpass.dto.gear.GearItemRequest;
import com.babygearpass.dto.gear.GearItemStatusRequest;
import com.babygearpass.service.GearItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gearitems")
@RequiredArgsConstructor
public class GearItemController {

    private final GearItemService gearItemService;

    @GetMapping
    public ApiResponse<PageResponse<GearItemDTO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String condition,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<GearItemDTO> result = gearItemService.getAllGearItems(keyword, category, status, condition, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @PostMapping
    public ApiResponse<GearItemDTO> create(@RequestBody @Valid GearItemRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(gearItemService.createGearItem(username, request));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<GearItemDTO>> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<GearItemDTO> result = gearItemService.getMyGearItems(username, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<GearItemDTO> get(@PathVariable Long id) {
        return ApiResponse.success(gearItemService.getGearItemById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<GearItemDTO> update(@PathVariable Long id, @RequestBody @Valid GearItemRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(gearItemService.updateGearItem(id, username, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        gearItemService.deleteGearItem(id, username);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<GearItemDTO> updateStatus(@PathVariable Long id, @RequestBody @Valid GearItemStatusRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(gearItemService.updateGearItemStatus(id, username, request));
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
