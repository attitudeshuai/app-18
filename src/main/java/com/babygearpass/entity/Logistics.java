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
@Table(name = "logistics")
public class Logistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"passwordHash"})
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handover_id", nullable = false, unique = true)
    private GearHandover handover;

    @Column(name = "tracking_number", nullable = false)
    private String trackingNumber;

    @Column(name = "express_company_code", nullable = false)
    private String expressCompanyCode;

    @Column(name = "express_company_name", nullable = false)
    private String expressCompanyName;

    @Column(name = "current_status", nullable = false)
    private String currentStatus = "Pending";

    @Column(name = "current_location")
    private String currentLocation;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "sender_phone")
    private String senderPhone;

    @Column(name = "sender_address")
    private String senderAddress;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "receiver_phone")
    private String receiverPhone;

    @Column(name = "receiver_address")
    private String receiverAddress;

    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;

    @Column(name = "sync_status", nullable = false)
    private String syncStatus = "Pending";

    @Column(name = "sync_fail_count", nullable = false)
    private Integer syncFailCount = 0;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "logistics", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LogisticsStatusLog> statusLogs;
}
