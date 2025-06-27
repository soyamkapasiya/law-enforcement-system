package com.poc.case_ingestion_service.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Evidence {
    private String evidenceId;
    private String type; // PHYSICAL, DIGITAL, TESTIMONIAL
    private String description;
    private String collectedBy;
    private LocalDateTime collectedAt;
}
