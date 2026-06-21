package com.babygearpass.service;

import com.babygearpass.dto.wishlist.*;
import com.babygearpass.entity.*;
import com.babygearpass.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistMatchRepository wishlistMatchRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final GearCategoryRepository gearCategoryRepository;
    private final GearItemRepository gearItemRepository;

    public Page<WishlistDTO> getAllWishlists(String city, Long categoryId, String status, String keyword, Pageable pageable) {
        Page<Wishlist> wishlists = wishlistRepository.findByFilters(city, categoryId, status, keyword, pageable);
        return wishlists.map(this::toWishlistDTO);
    }

    public Page<WishlistDTO> getMyWishlists(String username, Pageable pageable) {
        User user = getUserByUsername(username);
        Page<Wishlist> wishlists = wishlistRepository.findByUserId(user.getId(), pageable);
        return wishlists.map(this::toWishlistDTO);
    }

    public WishlistDTO getWishlistById(Long id) {
        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "心愿单不存在"));
        return toWishlistDTO(wishlist);
    }

    @Transactional
    public WishlistDTO createWishlist(String username, WishlistRequest request) {
        User user = getUserByUsername(username);
        GearCategory category = gearCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在"));

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setTitle(request.getTitle());
        wishlist.setCategory(category);
        wishlist.setExpectedCondition(request.getExpectedCondition());
        wishlist.setCity(request.getCity());
        wishlist.setAcceptableWearLevel(request.getAcceptableWearLevel());
        wishlist.setDescription(request.getDescription());
        wishlist.setStatus("Open");

        wishlist = wishlistRepository.save(wishlist);
        return toWishlistDTO(wishlist);
    }

    @Transactional
    public WishlistDTO updateWishlist(Long id, String username, WishlistRequest request) {
        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "心愿单不存在"));

        if (!wishlist.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此心愿单");
        }

        if (!"Open".equals(wishlist.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "心愿单已关闭，无法修改");
        }

        GearCategory category = gearCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在"));

        wishlist.setTitle(request.getTitle());
        wishlist.setCategory(category);
        wishlist.setExpectedCondition(request.getExpectedCondition());
        wishlist.setCity(request.getCity());
        wishlist.setAcceptableWearLevel(request.getAcceptableWearLevel());
        wishlist.setDescription(request.getDescription());

        wishlist = wishlistRepository.save(wishlist);
        return toWishlistDTO(wishlist);
    }

    @Transactional
    public void deleteWishlist(Long id, String username) {
        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "心愿单不存在"));

        if (!wishlist.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此心愿单");
        }

        wishlistRepository.delete(wishlist);
    }

    @Transactional
    public WishlistMatchDTO createMatch(String username, WishlistMatchRequest request) {
        User provider = getUserByUsername(username);
        Wishlist wishlist = wishlistRepository.findById(request.getWishlistId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "心愿单不存在"));

        if (!"Open".equals(wishlist.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "心愿单已关闭，无法匹配");
        }

        if (wishlist.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能匹配自己的心愿单");
        }

        if (wishlistMatchRepository.existsByWishlistIdAndProviderId(wishlist.getId(), provider.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "您已对此心愿单提交过匹配申请");
        }

        GearItem gearItem = null;
        if (request.getGearItemId() != null) {
            gearItem = gearItemRepository.findById(request.getGearItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用品不存在"));
            if (!gearItem.getOwner().getUsername().equals(username)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此用品");
            }
            if (!"Available".equals(gearItem.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用品状态不可用");
            }
        }

        WishlistMatch match = new WishlistMatch();
        match.setWishlist(wishlist);
        match.setProvider(provider);
        match.setGearItem(gearItem);
        match.setMessage(request.getMessage());
        match.setStatus("Pending");
        match.setIsAccepted(false);

        match = wishlistMatchRepository.save(match);

        createNotification(wishlist.getUser(), "NewMatch", "新的匹配申请",
                "用户 " + provider.getUsername() + " 对您的心愿单 \"" + wishlist.getTitle() + "\" 提交了匹配申请",
                wishlist, match);

        return toWishlistMatchDTO(match);
    }

    public Page<WishlistMatchDTO> getWishlistMatches(Long wishlistId, Pageable pageable) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "心愿单不存在"));
        Page<WishlistMatch> matches = wishlistMatchRepository.findByWishlistId(wishlistId, pageable);
        return matches.map(this::toWishlistMatchDTO);
    }

    public Page<WishlistMatchDTO> getMyProvidedMatches(String username, Pageable pageable) {
        User user = getUserByUsername(username);
        Page<WishlistMatch> matches = wishlistMatchRepository.findByProviderId(user.getId(), pageable);
        return matches.map(this::toWishlistMatchDTO);
    }

    public Page<WishlistMatchDTO> getMyReceivedMatches(String username, Pageable pageable) {
        User user = getUserByUsername(username);
        Page<WishlistMatch> matches = wishlistMatchRepository.findByWishlistUserId(user.getId(), pageable);
        return matches.map(this::toWishlistMatchDTO);
    }

    @Transactional
    public WishlistMatchDTO updateMatchStatus(Long matchId, String username, WishlistMatchStatusRequest request) {
        WishlistMatch match = wishlistMatchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "匹配记录不存在"));

        if (!match.getWishlist().getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此匹配记录");
        }

        if (!"Pending".equals(match.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "匹配记录已处理，无法修改状态");
        }

        match.setStatus(request.getStatus());
        if (request.getIsAccepted() != null) {
            match.setIsAccepted(request.getIsAccepted());
        }

        if (Boolean.TRUE.equals(match.getIsAccepted()) && "Accepted".equals(request.getStatus())) {
            closeWishlist(match.getWishlist(), match);

            createNotification(match.getProvider(), "MatchAccepted", "匹配已接受",
                    "您对心愿单 \"" + match.getWishlist().getTitle() + "\" 的匹配申请已被接受，请联系对方完成交接",
                    match.getWishlist(), match);
        } else if ("Rejected".equals(request.getStatus())) {
            createNotification(match.getProvider(), "MatchRejected", "匹配已拒绝",
                    "您对心愿单 \"" + match.getWishlist().getTitle() + "\" 的匹配申请已被拒绝",
                    match.getWishlist(), match);
        }

        match = wishlistMatchRepository.save(match);
        return toWishlistMatchDTO(match);
    }

    private void closeWishlist(Wishlist wishlist, WishlistMatch acceptedMatch) {
        wishlist.setStatus("Fulfilled");
        if (acceptedMatch.getGearItem() != null) {
            wishlist.setMatchedGearItemId(acceptedMatch.getGearItem().getId());
        }
        wishlistRepository.save(wishlist);

        for (WishlistMatch otherMatch : wishlistMatchRepository.findByWishlistId(wishlist.getId())) {
            if (!otherMatch.getId().equals(acceptedMatch.getId()) && "Pending".equals(otherMatch.getStatus())) {
                otherMatch.setStatus("Rejected");
                otherMatch.setIsAccepted(false);
                wishlistMatchRepository.save(otherMatch);

                createNotification(otherMatch.getProvider(), "MatchClosed", "心愿单已匹配",
                        "心愿单 \"" + wishlist.getTitle() + "\" 已与其他用户匹配成功，您的申请自动关闭",
                        wishlist, otherMatch);
            }
        }
    }

    private void createNotification(User user, String type, String title, String content, Wishlist wishlist, WishlistMatch match) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setWishlist(wishlist);
        notification.setWishlistMatch(match);
        notification.setStatus("Unread");
        notificationRepository.save(notification);
    }

    public Page<NotificationDTO> getMyNotifications(String username, String status, Pageable pageable) {
        User user = getUserByUsername(username);
        Page<Notification> notifications;
        if (status != null && !status.isBlank()) {
            notifications = notificationRepository.findByUserIdAndStatus(user.getId(), status, pageable);
        } else {
            notifications = notificationRepository.findByUserId(user.getId(), pageable);
        }
        return notifications.map(this::toNotificationDTO);
    }

    public long getUnreadNotificationCount(String username) {
        User user = getUserByUsername(username);
        return notificationRepository.countByUserIdAndStatus(user.getId(), "Unread");
    }

    @Transactional
    public NotificationDTO markNotificationAsRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "通知不存在"));

        if (!notification.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此通知");
        }

        notification.setStatus("Read");
        notification = notificationRepository.save(notification);
        return toNotificationDTO(notification);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
    }

    private WishlistDTO toWishlistDTO(Wishlist wishlist) {
        return new WishlistDTO(
                wishlist.getId(),
                wishlist.getUser().getId(),
                wishlist.getUser().getUsername(),
                wishlist.getTitle(),
                wishlist.getCategory().getId(),
                wishlist.getCategory().getName(),
                wishlist.getExpectedCondition(),
                wishlist.getCity(),
                wishlist.getAcceptableWearLevel(),
                wishlist.getDescription(),
                wishlist.getStatus(),
                wishlist.getMatchedGearItemId(),
                wishlist.getCreatedAt(),
                wishlist.getUpdatedAt()
        );
    }

    private WishlistMatchDTO toWishlistMatchDTO(WishlistMatch match) {
        return new WishlistMatchDTO(
                match.getId(),
                match.getWishlist().getId(),
                match.getWishlist().getTitle(),
                match.getProvider().getId(),
                match.getProvider().getUsername(),
                match.getGearItem() != null ? match.getGearItem().getId() : null,
                match.getGearItem() != null ? match.getGearItem().getTitle() : null,
                match.getStatus(),
                match.getMessage(),
                match.getIsAccepted(),
                match.getCreatedAt()
        );
    }

    private NotificationDTO toNotificationDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getUser().getId(),
                notification.getUser().getUsername(),
                notification.getWishlist() != null ? notification.getWishlist().getId() : null,
                notification.getWishlistMatch() != null ? notification.getWishlistMatch().getId() : null,
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getStatus(),
                notification.getCreatedAt()
        );
    }
}
