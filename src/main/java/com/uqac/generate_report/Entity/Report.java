package com.uqac.generate_report.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @JoinColumn(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "report_name", length = 255)
    private String reportName;

    @Lob
    @Column(name = "encrypted_file", length = 255)
    private byte[] encryptedFile;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "trigger_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private java.time.Instant triggerDate = java.time.Instant.now();
}
