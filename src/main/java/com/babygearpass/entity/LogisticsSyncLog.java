package com.babygearpass.entity;

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
@Table(name = "logistics_sync_logs")
public class LogisticsSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "logistics_id")
    private Logistics logistics;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "express_company_code")
    private String expressCompanyCode;

    @Column(name = "sync_type", nullable = false)
    private String syncType = "Auto";

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess = false;

    @Column(name = "error_code")
    private String errorCode;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String requestDetail;

    @Column(columnDefinition = "TEXT")
    private String responseDetail;

    @Column(name = "sync_start_time")
    private LocalDateTime syncStartTime;

    @Column(name = "sync_end_time")
    private LocalDateTime syncEndTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
