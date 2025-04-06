package com.uqac.generate_report.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "api.externe")
public class ApiProperties {
    private String url;
    private String path;

    // Getter & Setter
}
