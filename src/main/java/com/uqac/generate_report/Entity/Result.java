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

    @Lob
    @Column(name = "step1", nullable = true, columnDefinition = "LONGTEXT")
    private String step1;

    @Lob
    @Column(name = "step2", nullable = true, columnDefinition = "LONGTEXT")
    private String step2;

    @Lob
    @Column(name = "step3", nullable = true, columnDefinition = "LONGTEXT")
    private String step3;

    @Lob
    @Column(name = "step4", nullable = true, columnDefinition = "LONGTEXT")
    private String step4;
}
