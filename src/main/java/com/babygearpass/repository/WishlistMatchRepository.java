package com.babygearpass.repository;

import com.babygearpass.entity.WishlistMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistMatchRepository extends JpaRepository<WishlistMatch, Long> {

    Page<WishlistMatch> findByWishlistId(Long wishlistId, Pageable pageable);

    Page<WishlistMatch> findByProviderId(Long providerId, Pageable pageable);

    Page<WishlistMatch> findByWishlistUserId(Long userId, Pageable pageable);

    List<WishlistMatch> findByWishlistId(Long wishlistId);

    Optional<WishlistMatch> findByWishlistIdAndProviderId(Long wishlistId, Long providerId);

    boolean existsByWishlistIdAndProviderId(Long wishlistId, Long providerId);
}
