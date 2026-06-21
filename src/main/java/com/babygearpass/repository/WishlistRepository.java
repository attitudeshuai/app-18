package com.babygearpass.repository;

import com.babygearpass.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Page<Wishlist> findByUserId(Long userId, Pageable pageable);

    Page<Wishlist> findByStatus(String status, Pageable pageable);

    Page<Wishlist> findByCity(String city, Pageable pageable);

    Page<Wishlist> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Wishlist> findByCityAndCategoryId(String city, Long categoryId, Pageable pageable);

    Page<Wishlist> findByCityAndStatus(String city, String status, Pageable pageable);

    Page<Wishlist> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

    Page<Wishlist> findByCityAndCategoryIdAndStatus(String city, Long categoryId, String status, Pageable pageable);

    @Query("SELECT w FROM Wishlist w WHERE " +
           "(:city IS NULL OR w.city = :city) AND " +
           "(:categoryId IS NULL OR w.category.id = :categoryId) AND " +
           "(:status IS NULL OR w.status = :status) AND " +
           "(:keyword IS NULL OR w.title LIKE %:keyword% OR w.description LIKE %:keyword%)")
    Page<Wishlist> findByFilters(
            @Param("city") String city,
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable);
}
