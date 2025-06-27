package com.poc.case_ingestion_service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Evidence {
    private String evidenceId;
    private String type; // PHYSICAL, DIGITAL, TESTIMONIAL
    private String description;
    private String collectedBy;
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime collectedAt;
}
