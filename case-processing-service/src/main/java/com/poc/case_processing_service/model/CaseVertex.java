package com.poc.case_processing_service.model;

import com.arangodb.entity.DocumentField;
import lombok.Data;

@Data
public class CaseVertex {

    @DocumentField(DocumentField.Type.KEY)
    private String key;
    private String caseId;
    private String caseType;
    private String status;
    private String reportedAt;
    private String description;
    private String reportingOfficer;
}
