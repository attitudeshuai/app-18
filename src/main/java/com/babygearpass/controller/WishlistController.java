package com.babygearpass.controller;

import com.babygearpass.dto.common.ApiResponse;
import com.babygearpass.dto.common.PageResponse;
import com.babygearpass.dto.wishlist.WishlistDTO;
import com.babygearpass.dto.wishlist.WishlistMatchDTO;
import com.babygearpass.dto.wishlist.WishlistMatchRequest;
import com.babygearpass.dto.wishlist.WishlistMatchStatusRequest;
import com.babygearpass.dto.wishlist.WishlistRequest;
import com.babygearpass.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ApiResponse<PageResponse<WishlistDTO>> list(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<WishlistDTO> result = wishlistService.getAllWishlists(city, categoryId, status, keyword, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<WishlistDTO>> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<WishlistDTO> result = wishlistService.getMyWishlists(username, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<WishlistDTO> get(@PathVariable Long id) {
        return ApiResponse.success(wishlistService.getWishlistById(id));
    }

    @PostMapping
    public ApiResponse<WishlistDTO> create(@RequestBody @Valid WishlistRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(wishlistService.createWishlist(username, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<WishlistDTO> update(@PathVariable Long id, @RequestBody @Valid WishlistRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(wishlistService.updateWishlist(id, username, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        wishlistService.deleteWishlist(id, username);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/matches")
    public ApiResponse<PageResponse<WishlistMatchDTO>> getMatches(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<WishlistMatchDTO> result = wishlistService.getWishlistMatches(id, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @PostMapping("/matches")
    public ApiResponse<WishlistMatchDTO> createMatch(@RequestBody @Valid WishlistMatchRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(wishlistService.createMatch(username, request));
    }

    @GetMapping("/matches/provided")
    public ApiResponse<PageResponse<WishlistMatchDTO>> getProvidedMatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<WishlistMatchDTO> result = wishlistService.getMyProvidedMatches(username, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/matches/received")
    public ApiResponse<PageResponse<WishlistMatchDTO>> getReceivedMatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<WishlistMatchDTO> result = wishlistService.getMyReceivedMatches(username, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @PatchMapping("/matches/{id}/status")
    public ApiResponse<WishlistMatchDTO> updateMatchStatus(
            @PathVariable Long id,
            @RequestBody @Valid WishlistMatchStatusRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(wishlistService.updateMatchStatus(id, username, request));
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
