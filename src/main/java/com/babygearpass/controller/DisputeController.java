package com.babygearpass.controller;

import com.babygearpass.dto.common.ApiResponse;
import com.babygearpass.dto.common.PageResponse;
import com.babygearpass.dto.dispute.ArbitrationRecordDTO;
import com.babygearpass.dto.dispute.ArbitrationRequest;
import com.babygearpass.dto.dispute.DisputeDTO;
import com.babygearpass.dto.dispute.DisputeEvidenceDTO;
import com.babygearpass.dto.dispute.DisputeEvidenceRequest;
import com.babygearpass.dto.dispute.DisputeRequest;
import com.babygearpass.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<DisputeDTO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<DisputeDTO> result = disputeService.getAllDisputes(status, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<DisputeDTO>> myDisputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<DisputeDTO> result = disputeService.getMyDisputes(username, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<DisputeDTO> get(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(disputeService.getDisputeById(id, username));
    }

    @GetMapping("/{id}/evidences")
    public ApiResponse<List<DisputeEvidenceDTO>> getEvidences(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(disputeService.getDisputeEvidences(id, username));
    }

    @GetMapping("/{id}/arbitration-records")
    public ApiResponse<List<ArbitrationRecordDTO>> getArbitrationRecords(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(disputeService.getArbitrationRecords(id, username));
    }

    @PostMapping
    public ApiResponse<DisputeDTO> initiate(@RequestBody @Valid DisputeRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(disputeService.initiateDispute(username, request));
    }

    @PostMapping("/{disputeId}/evidences")
    public ApiResponse<DisputeEvidenceDTO> uploadEvidence(
            @PathVariable Long disputeId,
            @RequestBody @Valid DisputeEvidenceRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(disputeService.uploadEvidence(disputeId, username, request));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DisputeDTO> accept(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(disputeService.adminAcceptDispute(id, username));
    }

    @PostMapping("/{id}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DisputeDTO> freeze(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(disputeService.adminFreezeDispute(id, username, reason));
    }

    @PostMapping("/{id}/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DisputeDTO> unfreeze(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(disputeService.adminUnfreezeDispute(id, username));
    }

    @PostMapping("/{id}/arbitrate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DisputeDTO> arbitrate(
            @PathVariable Long id,
            @RequestBody @Valid ArbitrationRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(disputeService.arbitrate(id, username, request));
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
