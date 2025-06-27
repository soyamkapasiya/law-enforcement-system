package com.poc.case_processing_service.model;

import com.arangodb.serde.jackson.Key;
import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.From;
import com.arangodb.springframework.annotation.To;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("case_person") // Collection name for edges
public class CasePersonEdge {

    @Key
    private String key;

    @From
    private String from;

    @To
    private String to;
    private String role;
    private String relationshipType;

}