package com.babygearpass.service;

import com.babygearpass.dto.story.StoryDTO;
import com.babygearpass.dto.story.StoryRequest;
import com.babygearpass.entity.GearItem;
import com.babygearpass.entity.GearStory;
import com.babygearpass.entity.User;
import com.babygearpass.repository.GearItemRepository;
import com.babygearpass.repository.GearStoryRepository;
import com.babygearpass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GearStoryService {

    private final GearStoryRepository gearStoryRepository;
    private final GearItemRepository gearItemRepository;
    private final UserRepository userRepository;

    public Page<StoryDTO> getAllStories(String keyword, Long gearItemId, Pageable pageable) {
        if (gearItemId != null) {
            return gearStoryRepository.findByGearItemId(gearItemId, pageable).map(this::toDTO);
        }
        if (keyword != null && !keyword.isBlank()) {
            return gearStoryRepository.findByContentContainingIgnoreCase(keyword, pageable).map(this::toDTO);
        }
        return gearStoryRepository.findAll(pageable).map(this::toDTO);
    }

    public StoryDTO getStoryById(Long id) {
        GearStory story = gearStoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "故事不存在"));
        return toDTO(story);
    }

    public StoryDTO createStory(String username, StoryRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        GearItem gearItem = gearItemRepository.findById(request.getGearItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用品不存在"));

        GearStory story = new GearStory();
        story.setUser(user);
        story.setGearItem(gearItem);
        story.setContent(request.getContent());
        story.setPhotos(request.getPhotos());

        gearStoryRepository.save(story);
        return toDTO(story);
    }

    public StoryDTO updateStory(Long id, String username, StoryRequest request) {
        GearStory story = gearStoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "故事不存在"));

        if (!story.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此故事");
        }

        GearItem gearItem = gearItemRepository.findById(request.getGearItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用品不存在"));

        story.setGearItem(gearItem);
        story.setContent(request.getContent());
        story.setPhotos(request.getPhotos());

        gearStoryRepository.save(story);
        return toDTO(story);
    }

    public void deleteStory(Long id, String username) {
        GearStory story = gearStoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "故事不存在"));

        if (!story.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此故事");
        }

        gearStoryRepository.delete(story);
    }

    public Page<StoryDTO> getMyStories(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        return gearStoryRepository.findByUserId(user.getId(), pageable).map(this::toDTO);
    }

    private StoryDTO toDTO(GearStory story) {
        return new StoryDTO(
                story.getId(),
                story.getGearItem().getId(),
                story.getGearItem().getTitle(),
                story.getUser().getId(),
                story.getUser().getUsername(),
                story.getContent(),
                story.getPhotos(),
                story.getCreatedAt()
        );
    }
}
