package com.poc.case_ingestion_service.service;

import com.poc.case_ingestion_service.model.CaseReport;
import lombok.RequiredArgsConstructor;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CaseIngestionService {

    private ProducerTemplate producerTemplate;

    public void processCase(CaseReport caseReport) {
        // Send to Camel route for processing
        producerTemplate.sendBody("direct:processCase", caseReport);
    }

    public CaseReport getCaseById(String caseId) {
        // Use requestBody to get a response from the route
        return producerTemplate.requestBody("direct:getCaseById", caseId, CaseReport.class);
    }

    public List<?> searchCases(Map<String, String> searchParams) {
        // Implement search functionality
        return producerTemplate.requestBody("direct:searchCases", searchParams, List.class);
    }

    public void processBulkCases(List<CaseReport> cases) {
        for (CaseReport caseReport : cases) {
            processCase(caseReport);
        }
    }
}
