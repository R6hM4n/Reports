package az.projectdailyreport.projectdailyreport.repository;

import az.projectdailyreport.projectdailyreport.dto.dailyreport.DailyReportUser;
import az.projectdailyreport.projectdailyreport.model.DailyReport;
import az.projectdailyreport.projectdailyreport.model.Project;
import az.projectdailyreport.projectdailyreport.model.User;
import az.projectdailyreport.projectdailyreport.dto.project.ProjectDTO;
import org.checkerframework.common.util.report.qual.ReportCreation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ReportCreation
public interface DailyReportRepository  extends JpaRepository<DailyReport,Long> {

    Optional<DailyReport> findByProjectAndUserAndLocalDateTimeBetween(Project project, User user, LocalDateTime start, LocalDateTime end);

    @Query("SELECT dr FROM DailyReport dr " +
            "WHERE (:firstNames IS NULL OR dr.firstName IN :firstNames) " +
            "AND ((:startDate IS NOT NULL AND :endDate IS NOT NULL AND DATE(dr.localDateTime) BETWEEN :startDate AND :endDate) " +
            "OR (:startDate IS NOT NULL AND :endDate IS NULL AND DATE(dr.localDateTime) >= :startDate) " +
            "OR (:startDate IS NULL AND :endDate IS NOT NULL AND DATE(dr.localDateTime) <= :endDate))")
    List<DailyReport> findByFirstNameInAndLocalDateTimeBetween(
            @Param("firstNames") List<String> firstNames,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
    boolean existsByProjectAndUserAndLocalDateTimeBetween(Project project, User user, LocalDateTime start, LocalDateTime end);

    List<DailyReport> findByUser_Id(Long userId);
    @Query("SELECT dr FROM DailyReport dr " +
            "WHERE (:userId IS NULL OR dr.user.id = :userId) " +
            "AND ((:startDate IS NULL AND :endDate IS NULL) OR " +
            "(:startDate IS NULL AND :endDate IS NOT NULL AND DATE(dr.localDateTime) <= :endDate) OR " +
            "(:startDate IS NOT NULL AND :endDate IS NULL AND DATE(dr.localDateTime) >= :startDate) OR " +
            "(DATE(dr.localDateTime) BETWEEN :startDate AND :endDate))")
    List<DailyReport> findUserReportsBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );


}

