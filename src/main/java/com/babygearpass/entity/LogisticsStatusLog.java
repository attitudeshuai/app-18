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
@Table(name = "logistics_status_logs")
public class LogisticsStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "logistics_id", nullable = false)
    private Logistics logistics;

    @Column(name = "status_code", nullable = false)
    private String statusCode;

    @Column(name = "status_name", nullable = false)
    private String statusName;

    @Column(nullable = false)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "is_signature_required")
    private Boolean isSignatureRequired = false;

    @Column(name = "operator_name")
    private String operatorName;

    @Column(name = "operator_phone")
    private String operatorPhone;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
