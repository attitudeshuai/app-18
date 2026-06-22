package com.babygearpass.service;

import com.babygearpass.dto.gear.GearItemDTO;
import com.babygearpass.dto.gear.GearItemRequest;
import com.babygearpass.dto.gear.GearItemStatusRequest;
import com.babygearpass.entity.GearCategory;
import com.babygearpass.entity.GearItem;
import com.babygearpass.entity.QualityCheck;
import com.babygearpass.entity.User;
import com.babygearpass.repository.GearCategoryRepository;
import com.babygearpass.repository.GearItemRepository;
import com.babygearpass.repository.QualityCheckRepository;
import com.babygearpass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GearItemService {

    private final GearItemRepository gearItemRepository;
    private final UserRepository userRepository;
    private final GearCategoryRepository gearCategoryRepository;
    private final QualityCheckRepository qualityCheckRepository;

    public Page<GearItemDTO> getAllGearItems(String keyword, String category, String status, String condition, Pageable pageable) {
        Page<GearItem> items;

        if (keyword != null && !keyword.isBlank()) {
            items = gearItemRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else if (status != null && !status.isBlank()) {
            items = gearItemRepository.findByStatus(status, pageable);
        } else if (category != null && !category.isBlank()) {
            Long categoryId = Long.parseLong(category);
            items = gearItemRepository.findByCategoryId(categoryId, pageable);
        } else {
            items = gearItemRepository.findAll(pageable);
        }

        return items.map(this::toDTO);
    }

    public GearItemDTO getGearItemById(Long id) {
        GearItem item = gearItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用品不存在"));
        return toDTO(item);
    }

    public GearItemDTO createGearItem(String username, GearItemRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        GearCategory category = gearCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在"));

        GearItem item = new GearItem();
        item.setOwner(user);
        item.setCategory(category);
        item.setTitle(request.getTitle());
        item.setCondition(request.getCondition());
        item.setBrand(request.getBrand());
        item.setSuitableAge(request.getSuitableAge());
        item.setDescription(request.getDescription());
        item.setPhotos(request.getPhotos());
        item.setPriceType(request.getPriceType());
        item.setPrice(request.getPrice());

        gearItemRepository.save(item);
        return toDTO(item);
    }

    public GearItemDTO updateGearItem(Long id, String username, GearItemRequest request) {
        GearItem item = gearItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用品不存在"));

        if (!item.getOwner().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此用品");
        }

        GearCategory category = gearCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在"));

        item.setCategory(category);
        item.setTitle(request.getTitle());
        item.setCondition(request.getCondition());
        item.setBrand(request.getBrand());
        item.setSuitableAge(request.getSuitableAge());
        item.setDescription(request.getDescription());
        item.setPhotos(request.getPhotos());
        item.setPriceType(request.getPriceType());
        item.setPrice(request.getPrice());

        gearItemRepository.save(item);
        return toDTO(item);
    }

    public void deleteGearItem(Long id, String username) {
        GearItem item = gearItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用品不存在"));

        if (!item.getOwner().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此用品");
        }

        gearItemRepository.delete(item);
    }

    public GearItemDTO updateGearItemStatus(Long id, String username, GearItemStatusRequest request) {
        GearItem item = gearItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用品不存在"));

        if (!item.getOwner().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此用品");
        }

        item.setStatus(request.getStatus());
        gearItemRepository.save(item);
        return toDTO(item);
    }

    public Page<GearItemDTO> getMyGearItems(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        return gearItemRepository.findByOwnerId(user.getId(), pageable).map(this::toDTO);
    }

    private GearItemDTO toDTO(GearItem item) {
        String qcStatus = item.getQualityCheckStatus();
        Integer qcScore = item.getQualityScore();
        boolean certified = false;

        QualityCheck latestCheck = qualityCheckRepository.findTopByGearItemIdOrderByCreatedAtDesc(item.getId()).orElse(null);
        if (latestCheck != null) {
            qcStatus = latestCheck.getStatus();
            qcScore = latestCheck.getQualityScore();
            certified = "Approved".equals(latestCheck.getStatus());
        }

        return new GearItemDTO(
                item.getId(),
                item.getOwner().getId(),
                item.getOwner().getUsername(),
                item.getTitle(),
                item.getCategory() != null ? item.getCategory().getId() : null,
                item.getCategory() != null ? item.getCategory().getName() : null,
                item.getCondition(),
                item.getBrand(),
                item.getSuitableAge(),
                item.getDescription(),
                item.getPhotos(),
                item.getStatus(),
                item.getPriceType(),
                item.getPrice(),
                qcStatus,
                qcScore,
                certified,
                item.getCreatedAt()
        );
    }
}
