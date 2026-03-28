package com.konomusic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CurationItem Entity - Video chờ phê duyệt (dành cho Admin)
 */
@Entity
@Table(name = "curation_items", indexes = {
        @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String youtubeId;

    @Column(nullable = false)
    private String title;

    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurationStatus status = CurationStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    private LocalDateTime submittedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private LocalDateTime approvedAt;

    private String rejectionReason;

    public enum CurationStatus {
        DRAFT, APPROVED, REJECTED
    }

}

