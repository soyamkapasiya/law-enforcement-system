package com.poc.case_ingestion_service.controller;

import com.poc.case_ingestion_service.model.CaseReport;
import com.poc.case_ingestion_service.service.CaseIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseIngestionService caseIngestionService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitCase(@RequestBody CaseReport caseReport) {
        try {
            caseIngestionService.processCase(caseReport);
            return ResponseEntity.ok("Case submitted successfully: " + caseReport.getCaseId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing case: " + e.getMessage());
        }
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<?> getCaseById(@PathVariable String caseId) {
        try {
            CaseReport caseReport = caseIngestionService.getCaseById(caseId);
            return ResponseEntity.ok(caseReport);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving case: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Case Ingestion Service is running");
    }
}
