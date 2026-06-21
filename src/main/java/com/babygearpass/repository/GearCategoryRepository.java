package com.babygearpass.repository;

import com.babygearpass.entity.GearCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GearCategoryRepository extends JpaRepository<GearCategory, Long> {

    Optional<GearCategory> findByName(String name);

    Page<GearCategory> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
