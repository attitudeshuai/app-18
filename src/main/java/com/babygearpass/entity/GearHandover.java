package com.babygearpass.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gear_handovers")
public class GearHandover {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gear_item_id", nullable = false)
    private GearItem gearItem;

    @JsonIgnoreProperties({"passwordHash"})
    @ManyToOne
    @JoinColumn(name = "giver_id", nullable = false)
    private User giver;

    @JsonIgnoreProperties({"passwordHash"})
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "quality_check_id")
    private QualityCheck qualityCheck;

    @Column(name = "handover_date")
    private LocalDate handoverDate;

    private String location;

    @Column(nullable = false)
    private String status = "Pending";

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "escrow_status", nullable = false)
    private String escrowStatus = "None";

    @Column(name = "escrow_start_at")
    private LocalDateTime escrowStartAt;

    @Column(name = "escrow_end_at")
    private LocalDateTime escrowEndAt;

    @Column(name = "frozen_points", nullable = false)
    private Integer frozenPoints = 0;

    @Column(name = "confirmed_by_receiver")
    private Boolean confirmedByReceiver = false;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "has_dispute", nullable = false)
    private Boolean hasDispute = false;

    @Column(name = "is_frozen", nullable = false)
    private Boolean isFrozen = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "handover")
    private List<Dispute> disputes;

    @OneToOne(mappedBy = "handover", cascade = CascadeType.ALL)
    private Logistics logistics;
}
