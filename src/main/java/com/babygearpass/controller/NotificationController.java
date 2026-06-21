package com.babygearpass.controller;

import com.babygearpass.dto.common.ApiResponse;
import com.babygearpass.dto.common.PageResponse;
import com.babygearpass.dto.wishlist.NotificationDTO;
import com.babygearpass.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final WishlistService wishlistService;

    @GetMapping
    public ApiResponse<PageResponse<NotificationDTO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<NotificationDTO> result = wishlistService.getMyNotifications(username, status, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> getUnreadCount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        long count = wishlistService.getUnreadNotificationCount(username);
        return ApiResponse.success(Map.of("count", count));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationDTO> markAsRead(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(wishlistService.markNotificationAsRead(id, username));
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
