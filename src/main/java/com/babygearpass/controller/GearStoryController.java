package com.babygearpass.controller;

import com.babygearpass.dto.common.ApiResponse;
import com.babygearpass.dto.common.PageResponse;
import com.babygearpass.dto.story.StoryDTO;
import com.babygearpass.dto.story.StoryRequest;
import com.babygearpass.service.GearStoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gearstories")
@RequiredArgsConstructor
public class GearStoryController {

    private final GearStoryService gearStoryService;

    @GetMapping
    public ApiResponse<PageResponse<StoryDTO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long gearItemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<StoryDTO> result = gearStoryService.getAllStories(keyword, gearItemId, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @PostMapping
    public ApiResponse<StoryDTO> create(@RequestBody @Valid StoryRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(gearStoryService.createStory(username, request));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<StoryDTO>> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<StoryDTO> result = gearStoryService.getMyStories(username, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<StoryDTO> get(@PathVariable Long id) {
        return ApiResponse.success(gearStoryService.getStoryById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<StoryDTO> update(@PathVariable Long id, @RequestBody @Valid StoryRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(gearStoryService.updateStory(id, username, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        gearStoryService.deleteStory(id, username);
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
