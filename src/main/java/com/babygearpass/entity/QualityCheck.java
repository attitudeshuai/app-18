package com.babygearpass.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quality_checks")
public class QualityCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gear_item_id", nullable = false)
    private GearItem gearItem;

    @JsonIgnoreProperties({"passwordHash"})
    @ManyToOne
    @JoinColumn(name = "submitter_id", nullable = false)
    private User submitter;

    @JsonIgnoreProperties({"passwordHash"})
    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(nullable = false)
    private String status = "Pending";

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "supplement_deadline")
    private LocalDateTime supplementDeadline;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(name = "quality_score")
    private Integer qualityScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @OneToMany(mappedBy = "qualityCheck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QualityCheckMaterial> materials;
}
