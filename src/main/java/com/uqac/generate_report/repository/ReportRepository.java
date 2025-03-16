package com.uqac.generate_report.repository;


import com.uqac.generate_report.Entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * interface ReportRepository
 */
public interface ReportRepository extends JpaRepository<Report, UUID> {
    // Trouver tous les rapports d'un utilisateur donné par son ID
    List<Report> findByUserId(UUID userId);

    Optional<Report> findByReportId(Long reportId);



}


