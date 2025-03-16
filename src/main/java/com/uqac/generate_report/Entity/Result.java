package com.uqac.generate_report.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(name = "step1", nullable = true, length = 4000)
    private String step1;

    @Column(name = "step2", nullable = true, length = 4000)
    private String step2;

    @Column(name = "step3", nullable = true, length = 4000)
    private String step3;

    @Column(name = "step4", nullable = true, length = 4000)
    private String step4;
}
