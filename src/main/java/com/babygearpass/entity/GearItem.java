package com.babygearpass.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 母婴用品实体 - 用户发布的闲置母婴用品信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gear_items")
public class GearItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"passwordHash"})
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private GearCategory category;

    @Column(name = "`condition`", nullable = false)
    private String condition;

    private String brand;

    @Column(name = "suitable_age")
    private String suitableAge;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String photos;

    @Column(nullable = false)
    private String status = "Available";

    @Column(name = "price_type", nullable = false)
    private String priceType;

    private BigDecimal price;

    @Column(name = "quality_check_status")
    private String qualityCheckStatus = "NotChecked";

    @Column(name = "quality_score")
    private Integer qualityScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "gearItem")
    private List<QualityCheck> qualityChecks;
}
