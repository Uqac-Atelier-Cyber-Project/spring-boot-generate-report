package com.uqac.generate_report.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class ServiceRequest {
    private Long reportId;
}
