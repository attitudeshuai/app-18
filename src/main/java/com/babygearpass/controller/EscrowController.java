package com.babygearpass.controller;

import com.babygearpass.dto.common.ApiResponse;
import com.babygearpass.dto.escrow.ConfirmReceiptRequest;
import com.babygearpass.dto.escrow.EscrowDTO;
import com.babygearpass.dto.escrow.StartEscrowRequest;
import com.babygearpass.service.EscrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/escrow")
@RequiredArgsConstructor
public class EscrowController {

    private final EscrowService escrowService;

    @GetMapping("/{handoverId}")
    public ApiResponse<EscrowDTO> getEscrowInfo(@PathVariable Long handoverId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(escrowService.getEscrowInfo(handoverId, username));
    }

    @PostMapping("/{handoverId}/start")
    public ApiResponse<EscrowDTO> startEscrow(
            @PathVariable Long handoverId,
            @RequestBody @Valid StartEscrowRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(escrowService.startEscrow(handoverId, username, request));
    }

    @PostMapping("/{handoverId}/confirm")
    public ApiResponse<EscrowDTO> confirmReceipt(
            @PathVariable Long handoverId,
            @RequestBody @Valid ConfirmReceiptRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(escrowService.confirmReceipt(handoverId, username, request));
    }

    @PostMapping("/{handoverId}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<EscrowDTO> adminFreeze(
            @PathVariable Long handoverId,
            @RequestParam(required = false) String reason) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(escrowService.adminFreezeEscrow(handoverId, username, reason));
    }

    @PostMapping("/{handoverId}/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<EscrowDTO> adminUnfreeze(@PathVariable Long handoverId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(escrowService.adminUnfreezeEscrow(handoverId, username));
    }
}
