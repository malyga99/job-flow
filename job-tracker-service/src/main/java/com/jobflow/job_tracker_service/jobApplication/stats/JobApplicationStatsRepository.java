package com.jobflow.job_tracker_service.jobApplication.stats;

import com.jobflow.job_tracker_service.jobApplication.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JobApplicationStatsRepository extends JpaRepository<JobApplication, Long> {

    @Query(
            value = "SELECT COUNT(*) FROM job_applications WHERE user_id = :userId",
            nativeQuery = true
    )
    Long countAllByUserId(@Param("userId") Long userId);

    @Query(
            value = "SELECT COUNT(*) FROM job_applications WHERE user_id = :userId AND applied_at >= :from",
            nativeQuery = true
    )
    Long countSince(@Param("userId") Long userId, @Param("from") LocalDate from);

    @Query(
            value = "SELECT COUNT(DISTINCT LOWER(TRIM(company))) FROM job_applications WHERE user_id = :userId",
            nativeQuery = true
    )
    Long countUniqueCompanies(@Param("userId") Long userId);

    @Query(
            value = "SELECT status AS status, COUNT(*) AS total FROM job_applications WHERE user_id = :userId GROUP BY status",
            nativeQuery = true
    )
    List<StatusCountProjection> countByStatus(@Param("userId") Long userId);

    @Query(
            value = """
                    SELECT INITCAP(LOWER(TRIM(company))) AS company, COUNT(*) AS total
                    FROM job_applications
                    WHERE user_id = :userId
                    GROUP BY LOWER(TRIM(company))
                    ORDER BY total DESC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<PopularCompanyProjection> findTopCompany(@Param("userId") Long userId);

    @Query(
            value = """
                    SELECT INITCAP(LOWER(TRIM(position))) AS position, COUNT(*) AS total
                    FROM job_applications
                    WHERE user_id = :userId
                    GROUP BY LOWER(TRIM(position))
                    ORDER BY total DESC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<PopularPositionProjection> findTopPosition(@Param("userId") Long userId);
}
