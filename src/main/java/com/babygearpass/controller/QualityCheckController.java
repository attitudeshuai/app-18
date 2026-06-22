package com.babygearpass.controller;

import com.babygearpass.dto.common.ApiResponse;
import com.babygearpass.dto.common.PageResponse;
import com.babygearpass.dto.qualitycheck.*;
import com.babygearpass.service.QualityCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quality-checks")
@RequiredArgsConstructor
public class QualityCheckController {

    private final QualityCheckService qualityCheckService;

    @GetMapping
    public ApiResponse<PageResponse<QualityCheckDTO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<QualityCheckDTO> result = qualityCheckService.getAllQualityChecks(status, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<QualityCheckDTO>> myChecks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<QualityCheckDTO> result = qualityCheckService.getMyQualityChecks(username, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/review")
    public ApiResponse<PageResponse<QualityCheckDTO>> reviewChecks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<QualityCheckDTO> result = qualityCheckService.getReviewQualityChecks(username, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/gear-item/{gearItemId}")
    public ApiResponse<PageResponse<QualityCheckDTO>> getByGearItem(
            @PathVariable Long gearItemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<QualityCheckDTO> result = qualityCheckService.getQualityChecksByGearItem(gearItemId, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/gear-item/{gearItemId}/latest")
    public ApiResponse<QualityCheckDTO> getLatestByGearItem(@PathVariable Long gearItemId) {
        return ApiResponse.success(qualityCheckService.getLatestQualityCheckByGearItem(gearItemId));
    }

    @GetMapping("/{id}")
    public ApiResponse<QualityCheckDTO> get(@PathVariable Long id) {
        return ApiResponse.success(qualityCheckService.getQualityCheckById(id));
    }

    @GetMapping("/{id}/materials")
    public ApiResponse<List<QualityCheckMaterialDTO>> getMaterials(@PathVariable Long id) {
        return ApiResponse.success(qualityCheckService.getMaterialsByQualityCheckId(id));
    }

    @PostMapping
    public ApiResponse<QualityCheckDTO> submit(@RequestBody @Valid QualityCheckRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(qualityCheckService.submitQualityCheck(username, request));
    }

    @PutMapping("/{id}/review")
    public ApiResponse<QualityCheckDTO> review(@PathVariable Long id, @RequestBody @Valid QualityCheckReviewRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(qualityCheckService.reviewQualityCheck(id, username, request));
    }

    @PutMapping("/{id}/supplement")
    public ApiResponse<QualityCheckDTO> supplement(@PathVariable Long id, @RequestBody @Valid QualityCheckSupplementRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(qualityCheckService.supplementMaterials(id, username, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        qualityCheckService.deleteQualityCheck(id, username);
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
