package com.babygearpass.controller;

import com.babygearpass.dto.common.ApiResponse;
import com.babygearpass.dto.common.PageResponse;
import com.babygearpass.dto.handover.HandoverDTO;
import com.babygearpass.dto.handover.HandoverRequest;
import com.babygearpass.dto.handover.HandoverStatusRequest;
import com.babygearpass.service.GearHandoverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gearhandovers")
@RequiredArgsConstructor
public class GearHandoverController {

    private final GearHandoverService gearHandoverService;

    @GetMapping
    public ApiResponse<PageResponse<HandoverDTO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long gearItemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<HandoverDTO> result = gearHandoverService.getAllHandovers(keyword, status, gearItemId, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<HandoverDTO>> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<HandoverDTO> result = gearHandoverService.getMyHandovers(username, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @PostMapping
    public ApiResponse<HandoverDTO> create(@RequestBody @Valid HandoverRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(gearHandoverService.createHandover(username, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<HandoverDTO> get(@PathVariable Long id) {
        return ApiResponse.success(gearHandoverService.getHandoverById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<HandoverDTO> update(@PathVariable Long id, @RequestBody @Valid HandoverRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(gearHandoverService.updateHandover(id, username, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        gearHandoverService.deleteHandover(id, username);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<HandoverDTO> updateStatus(@PathVariable Long id, @RequestBody @Valid HandoverStatusRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(gearHandoverService.updateHandoverStatus(id, username, request));
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
