package com.poc.case_processing_service.model;

import com.arangodb.serde.jackson.Key;
import lombok.Data;

@Data
public class CaseVertex {

    @Key
    private String key;
    private String caseId;
    private String caseType;
    private String status;
    private String reportedAt;
    private String description;
    private String reportingOfficer;
}
