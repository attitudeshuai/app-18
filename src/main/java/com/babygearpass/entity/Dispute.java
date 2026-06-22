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
@Table(name = "disputes")
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "handover_id", nullable = false)
    private GearHandover handover;

    @JsonIgnoreProperties({"passwordHash"})
    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Column(nullable = false)
    private String status = "Pending";

    @Column(name = "dispute_type", nullable = false)
    private String disputeType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonIgnoreProperties({"passwordHash"})
    @ManyToOne
    @JoinColumn(name = "arbitrator_id")
    private User arbitrator;

    @Column(name = "arbitration_result")
    private String arbitrationResult;

    @Column(name = "arbitration_opinion", columnDefinition = "TEXT")
    private String arbitrationOpinion;

    @Column(name = "giver_credit_change")
    private Integer giverCreditChange = 0;

    @Column(name = "receiver_credit_change")
    private Integer receiverCreditChange = 0;

    @Column(name = "points_handle_result")
    private String pointsHandleResult;

    @Column(name = "arbitrated_at")
    private LocalDateTime arbitratedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "dispute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DisputeEvidence> evidences;

    @OneToMany(mappedBy = "dispute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArbitrationRecord> arbitrationRecords;
}
