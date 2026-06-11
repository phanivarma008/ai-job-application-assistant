package com.ai.jobassistant.repository;

import com.ai.jobassistant.model.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JobApplicationRepository
 * JPA data access for job_applications table in PostgreSQL.
 */
@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    Page<JobApplication> findByUserId(String userId, Pageable pageable);

    Page<JobApplication> findByUserIdAndStatus(
        String userId,
        JobApplication.ApplicationStatus status,
        Pageable pageable
    );

    List<JobApplication> findByUserIdOrderByCreatedAtDesc(String userId);

    // Find top scoring applications
    @Query("SELECT j FROM JobApplication j WHERE j.userId = :userId " +
           "AND j.atsScore IS NOT NULL ORDER BY j.atsScore DESC")
    List<JobApplication> findTopScoringApplications(@Param("userId") String userId);

    // Count by status for dashboard stats
    @Query("SELECT j.status, COUNT(j) FROM JobApplication j " +
           "WHERE j.userId = :userId GROUP BY j.status")
    List<Object[]> countByStatusForUser(@Param("userId") String userId);

    // Search by company name
    @Query("SELECT j FROM JobApplication j WHERE j.userId = :userId " +
           "AND LOWER(j.companyName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<JobApplication> searchByCompany(
        @Param("userId") String userId,
        @Param("query") String query,
        Pageable pageable
    );

    boolean existsByUserIdAndCompanyNameAndJobTitle(
        String userId, String companyName, String jobTitle
    );
}
