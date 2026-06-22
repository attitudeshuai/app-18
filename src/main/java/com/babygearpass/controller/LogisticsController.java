package com.babygearpass.controller;

import com.babygearpass.dto.common.ApiResponse;
import com.babygearpass.dto.common.PageResponse;
import com.babygearpass.dto.logistics.*;
import com.babygearpass.service.LogisticsService;
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
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;

    @PostMapping
    public ApiResponse<LogisticsDTO> create(@RequestBody @Valid LogisticsRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(logisticsService.createLogistics(username, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<LogisticsDTO> update(@PathVariable Long id, @RequestBody @Valid LogisticsRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(logisticsService.updateLogistics(id, username, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<LogisticsDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(logisticsService.getLogisticsById(id));
    }

    @GetMapping("/handover/{handoverId}")
    public ApiResponse<LogisticsDTO> getByHandoverId(@PathVariable Long handoverId) {
        return ApiResponse.success(logisticsService.getLogisticsByHandoverId(handoverId));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ApiResponse<LogisticsDTO> getByTrackingNumber(@PathVariable String trackingNumber) {
        return ApiResponse.success(logisticsService.getLogisticsByTrackingNumber(trackingNumber));
    }

    @GetMapping
    public ApiResponse<PageResponse<LogisticsDTO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String syncStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ApiResponse.success(logisticsService.getAllLogistics(status, syncStatus, pageable));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<LogisticsDTO>> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ApiResponse.success(logisticsService.getMyLogistics(username, pageable));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logisticsService.deleteLogistics(id, username);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/sync")
    public ApiResponse<LogisticsDTO> sync(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(logisticsService.syncLogistics(id, username));
    }

    @PostMapping("/handover/{handoverId}/sync")
    public ApiResponse<LogisticsDTO> syncByHandoverId(@PathVariable Long handoverId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(logisticsService.syncLogisticsByHandoverId(handoverId, username));
    }

    @PostMapping("/{id}/manual-update")
    public ApiResponse<LogisticsDTO> manualUpdate(
            @PathVariable Long id,
            @RequestBody LogisticsManualUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(logisticsService.updateLogisticsManually(id, username, request));
    }

    @GetMapping("/express-companies")
    public ApiResponse<List<ExpressCompanyDTO>> getExpressCompanies() {
        return ApiResponse.success(logisticsService.getSupportedExpressCompanies());
    }

    @GetMapping("/express-companies/identify")
    public ApiResponse<ExpressCompanyDTO> identifyExpressCompany(@RequestParam String trackingNumber) {
        return ApiResponse.success(logisticsService.identifyExpressCompany(trackingNumber));
    }

    @GetMapping("/sync-logs")
    public ApiResponse<PageResponse<LogisticsSyncLogDTO>> getSyncLogs(
            @RequestParam(required = false) Long logisticsId,
            @RequestParam(required = false) Boolean onlyFailed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ApiResponse.success(logisticsService.getSyncLogs(logisticsId, onlyFailed, pageable));
    }
}
