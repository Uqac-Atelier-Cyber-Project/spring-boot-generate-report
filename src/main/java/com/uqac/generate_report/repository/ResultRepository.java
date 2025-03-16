package com.uqac.generate_report.repository;

import com.uqac.generate_report.Entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Interface ResultRepository
 */
public interface ResultRepository extends JpaRepository<Result, UUID> {

    List<Result> findByReportId(Long reportId);

}
