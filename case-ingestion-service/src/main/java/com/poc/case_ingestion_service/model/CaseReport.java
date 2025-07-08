package com.poc.case_ingestion_service.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class CaseReport {
    @NotBlank
    private String caseId;

    @NotBlank
    private String caseType; // THEFT, ASSAULT, FRAUD, CYBERCRIME, DOMESTIC_VIOLENCE, etc.

    @NotBlank
    private String status; // OPEN, CLOSED, PENDING, UNDER_INVESTIGATION

    private String reportedAt;
    private String incidentDate;
    private String priority; // HIGH, MEDIUM, LOW, CRITICAL

    private Location location;
    private List<Person> involvedPersons;
    private List<Evidence> evidence;
    private List<Contact> contacts;

    private String description;
    private String reportingOfficer;
    private String assignedOfficer;
    private String department;
    private String precinct;

    // For tracking data lineage
    private String sourceFile;
    private String processedBy;
    private LocalDateTime processedAt;

    // Additional metadata
    private Map<String, Object> additionalData;
    private List<String> tags;
    private String severity;
    private Boolean isPublic;
}
