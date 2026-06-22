package com.babygearpass.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit_records")
public class CreditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"passwordHash"})
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "related_handover_id")
    private Long relatedHandoverId;

    @Column(name = "related_dispute_id")
    private Long relatedDisputeId;

    @Column(nullable = false)
    private String type;

    @Column(name = "score_change", nullable = false)
    private Integer scoreChange = 0;

    @Column(name = "before_score", nullable = false)
    private Integer beforeScore;

    @Column(name = "after_score", nullable = false)
    private Integer afterScore;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "operator_id")
    private Long operatorId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
