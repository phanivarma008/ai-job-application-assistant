package com.ai.jobassistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JobApplication Entity
 * Tracks every job application with AI-generated content and match scores.
 * Stored in PostgreSQL.
 */
@Entity
@Table(name = "job_applications", indexes = {
    @Index(name = "idx_user_id",    columnList = "user_id"),
    @Index(name = "idx_status",     columnList = "status"),
    @Index(name = "idx_company",    columnList = "company_name"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(name = "job_description", columnDefinition = "TEXT", nullable = false)
    private String jobDescription;

    @Column(name = "resume_text", columnDefinition = "TEXT")
    private String resumeText;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "ats_score")
    private Integer atsScore;  // 0-100

    @Column(name = "matched_keywords", columnDefinition = "TEXT")
    private String matchedKeywords; // comma-separated

    @Column(name = "missing_keywords", columnDefinition = "TEXT")
    private String missingKeywords; // comma-separated

    @Column(name = "interview_questions", columnDefinition = "TEXT")
    private String interviewQuestions; // JSON array

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    @Column(name = "job_url")
    private String jobUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public enum ApplicationStatus {
        DRAFT,
        READY,
        APPLIED,
        INTERVIEW_SCHEDULED,
        OFFER_RECEIVED,
        REJECTED,
        WITHDRAWN
    }
}
