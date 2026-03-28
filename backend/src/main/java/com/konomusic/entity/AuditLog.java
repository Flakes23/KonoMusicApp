package com.konomusic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AuditLog Entity - Ghi nhật ký hành động Admin
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_admin_id", columnList = "admin_id"),
        @Index(name = "idx_action", columnList = "action")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    private String action;

    private String entityType;

    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    private LocalDateTime createdAt = LocalDateTime.now();

}

