package com.poc.case_processing_service.model;

import com.arangodb.entity.DocumentField;
import lombok.Data;

@Data
public class CasePersonEdge {
    @DocumentField(DocumentField.Type.FROM)
    private String from;

    @DocumentField(DocumentField.Type.TO)
    private String to;

    private String role; // SUSPECT, VICTIM, WITNESS
    private String relationshipType;
}
