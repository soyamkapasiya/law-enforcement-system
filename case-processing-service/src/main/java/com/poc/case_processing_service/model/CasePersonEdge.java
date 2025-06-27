package com.poc.case_processing_service.model;

import com.arangodb.serde.jackson.From;
import com.arangodb.springframework.annotation.To;
import lombok.Data;

@Data
public class CasePersonEdge {

    @From
    private String from;

    @To
    private String to;

    private String role; // SUSPECT, VICTIM, WITNESS
    private String relationshipType;
}
