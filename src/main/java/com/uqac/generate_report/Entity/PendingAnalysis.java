package com.uqac.generate_report.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pending_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingAnalysis {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(name = "step1", nullable = false)
    private Boolean step1;

    @Column(name = "step2", nullable = false)
    private Boolean step2;

    @Column(name = "step3", nullable = false)
    private Boolean step3;

    @Column(name = "step4", nullable = false)
    private Boolean step4;

    @Column(name = "pdfPassword", nullable = false)
    private String pdfPassword;
}
