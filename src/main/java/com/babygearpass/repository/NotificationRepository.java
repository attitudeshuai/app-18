package com.babygearpass.repository;

import com.babygearpass.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    long countByUserIdAndStatus(Long userId, String status);
}
