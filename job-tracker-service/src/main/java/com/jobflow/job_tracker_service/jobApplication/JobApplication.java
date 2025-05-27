package com.jobflow.job_tracker_service.jobApplication;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "job_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "company", nullable = false, length = 200)
    private String company;

    @Column(name = "position", nullable = false, length = 100)
    private String position;

    @Column(name = "link", nullable = true, length = 500)
    private String link;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private Source source;

    @Column(name = "source_details", nullable = true, length = 100)
    private String sourceDetails;

    @Column(name = "salary_min", nullable = true)
    private Integer salaryMin;

    @Column(name = "salary_max", nullable = true)
    private Integer salaryMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = true)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "comment", nullable = true, length = 500)
    private String comment;

    @Column(name = "applied_at", nullable = false)
    private LocalDate appliedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
