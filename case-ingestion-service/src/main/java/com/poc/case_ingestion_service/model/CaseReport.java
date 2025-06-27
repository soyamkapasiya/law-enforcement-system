package com.poc.case_ingestion_service.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CaseReport {
    @NotBlank
    private String caseId;

    @NotBlank
    private String caseType; // THEFT, ASSAULT, FRAUD, etc.

    @NotBlank
    private String status; // OPEN, CLOSED, PENDING

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportedAt;

    private Location location;
    private List<Person> involvedPersons;
    private List<Evidence> evidence;
    private String description;
    private String reportingOfficer;
}
