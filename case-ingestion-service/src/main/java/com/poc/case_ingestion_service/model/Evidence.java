package com.poc.case_ingestion_service.model;

import lombok.Data;

import java.util.Map;

@Data
public class Evidence {
    private String evidenceId;
    private String type; // PHYSICAL, DIGITAL, TESTIMONIAL, FORENSIC, SURVEILLANCE
    private String description;
    private String collectedBy;
    private String collectedAt;
    private String storageLocation;
    private String chainOfCustody;
    private Boolean isSealed;
    private String fileReference;
    private Map<String, Object> metadata;
}