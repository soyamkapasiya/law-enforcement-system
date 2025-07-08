package com.poc.case_ingestion_service.controller;

import com.poc.case_ingestion_service.model.CaseReport;
import com.poc.case_ingestion_service.service.CaseIngestionService;
import com.poc.case_ingestion_service.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseIngestionService caseIngestionService;
    private final FileProcessingService fileProcessingService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitCase(@RequestBody CaseReport caseReport) {
        try {
            caseIngestionService.processCase(caseReport);
            return ResponseEntity.ok("Case submitted successfully: " + caseReport.getCaseId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing case: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "fileType", required = false) String fileType) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            List<CaseReport> cases = fileProcessingService.processFile(file, fileType);

            // Process each case through the pipeline
            for (CaseReport caseReport : cases) {
                caseIngestionService.processCase(caseReport);
            }

            return ResponseEntity.ok(Map.of(
                    "message", "File processed successfully",
                    "casesProcessed", cases.size(),
                    "fileName", Objects.requireNonNull(file.getOriginalFilename())
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing file: " + e.getMessage());
        }
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<?> bulkUpload(@RequestParam("files") MultipartFile[] files) {
        try {
            int totalCases = 0;
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    List<CaseReport> cases = fileProcessingService.processFile(file, null);
                    for (CaseReport caseReport : cases) {
                        caseIngestionService.processCase(caseReport);
                    }
                    totalCases += cases.size();
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Bulk upload completed successfully",
                    "filesProcessed", files.length,
                    "totalCasesProcessed", totalCases
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error in bulk upload: " + e.getMessage());
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

    @GetMapping("/search")
    public ResponseEntity<?> searchCases(@RequestParam Map<String, String> searchParams) {
        try {
            List<?> cases = caseIngestionService.searchCases(searchParams);
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error searching cases: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Case Ingestion Service is running");
    }
}
