package com.poc.case_processing_service.model;

import com.arangodb.serde.jackson.Key;
import com.arangodb.springframework.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("evidence")
public class EvidenceVertex {

    @Key
    private String key;

    private String evidenceId;
    private String type; // PHYSICAL, DIGITAL, TESTIMONIAL
    private String description;
    private String collectedBy;
    private String collectedAt;
}
