package com.poc.case_ingestion_service.controller;

import com.poc.case_ingestion_service.model.CaseReport;
import com.poc.case_ingestion_service.service.CaseIngestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    @Autowired
    private CaseIngestionService caseIngestionService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitCase(@Valid @RequestBody CaseReport caseReport) {
        try {
            caseIngestionService.processCase(caseReport);
            return ResponseEntity.ok("Case submitted successfully: " + caseReport.getCaseId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing case: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Case Ingestion Service is running");
    }
}
