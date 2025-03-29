package com.uqac.generate_report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IARequest {
    private String prompt;
}
