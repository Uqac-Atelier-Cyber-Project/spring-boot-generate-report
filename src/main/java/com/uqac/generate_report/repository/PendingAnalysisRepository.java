package com.uqac.generate_report.repository;

import com.uqac.generate_report.Entity.PendingAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Interface PendingAnalysisRepository
 */
public interface PendingAnalysisRepository extends JpaRepository<PendingAnalysis, Long> {

    Optional<PendingAnalysis> findByReportId(Long reportId);

}