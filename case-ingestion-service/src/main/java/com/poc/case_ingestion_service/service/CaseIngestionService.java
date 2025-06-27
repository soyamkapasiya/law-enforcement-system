package com.poc.case_ingestion_service.service;

import com.poc.case_ingestion_service.model.CaseReport;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CaseIngestionService {

    @Autowired
    private ProducerTemplate producerTemplate;

    public void processCase(CaseReport caseReport) {
        // Send to Camel route for processing
        producerTemplate.sendBody("direct:processCase", caseReport);
    }

    public CaseReport getCaseById(String caseId) {
        // Use requestBody to get a response from the route
        return producerTemplate.requestBody("direct:getCaseById", caseId, CaseReport.class);
    }
}
