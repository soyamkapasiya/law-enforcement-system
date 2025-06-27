package com.poc.case_processing_service.routes;

import com.poc.case_ingestion_service.model.CaseReport;
import com.poc.case_ingestion_service.model.Location;
import com.poc.case_ingestion_service.model.Person;
import com.poc.case_processing_service.model.CaseVertex;
import com.poc.case_processing_service.model.PersonVertex;
import com.poc.case_processing_service.model.LocationVertex;
import com.poc.case_processing_service.service.GraphDatabaseService;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaseProcessingRoute extends RouteBuilder {

    private final GraphDatabaseService graphDatabaseService;

    @Override
    public void configure() throws Exception {

        from("kafka:case-events?brokers=localhost:9092&groupId=case-processing-group")
                .routeId("kafka-consumer-route")
                .log("Received case from Kafka: ${body}")
                .unmarshal().json(JsonLibrary.Jackson, CaseReport.class)
                .to("direct:processCase");

        from("direct:processCase")
                .routeId("case-processing-route")
                .log("Processing case: ${body.caseId}")
                .doTry()
                .process(this::processAndStoreCase)
                .to("direct:detectPatterns")
                .log("Successfully processed case: ${body.caseId}")
                .doCatch(Exception.class)
                .log("Error processing case: ${exception.message}")
                .to("direct:handleError")
                .end();

        from("direct:detectPatterns")
                .routeId("pattern-detection-route")
                .log("Running pattern detection for case: ${body.caseId}")
                .process(this::detectPatterns)
                .process(this::generateAlerts);

        from("direct:handleError")
                .routeId("error-handling-route")
                .log("Handling error for case processing")
                .process(exchange -> {
                    Exception exception = exchange.getProperty("CamelExceptionCaught", Exception.class);
                    log.error("Case processing failed: {}", exception.getMessage(), exception);
                });
    }

    /**
     * Process case report and store all entities in graph database
     */
    private void processAndStoreCase(org.apache.camel.Exchange exchange) {
        CaseReport caseReport = exchange.getIn().getBody(CaseReport.class);

        CaseVertex caseVertex = createCaseVertex(caseReport);
        String caseKey = graphDatabaseService.saveCaseToGraph(caseVertex);

        if (caseReport.getInvolvedPersons() != null) {
            for (Person person : caseReport.getInvolvedPersons()) {
                PersonVertex personVertex = createPersonVertex(person);
                String personKey = graphDatabaseService.savePersonToGraph(personVertex);
                graphDatabaseService.createCasePersonRelationship(caseKey, personKey, person.getRole());
            }
        }

        if (caseReport.getLocation() != null) {
            LocationVertex locationVertex = createLocationVertex(caseReport.getLocation());
            graphDatabaseService.saveLocationToGraph(locationVertex);
        }

        exchange.getIn().setBody(caseVertex);
        log.info("Successfully stored case {} in graph database", caseKey);
    }

    /**
     * Run pattern detection queries
     */
    private void detectPatterns(org.apache.camel.Exchange exchange) {
        CaseVertex caseVertex = exchange.getIn().getBody(CaseVertex.class);

        try {
            detectSimilarCases(caseVertex);
            detectRecurringPersons(caseVertex);
            detectLocationHotspots();
            log.info("Pattern detection completed for case: {}", caseVertex.getCaseId());
        } catch (Exception e) {
            log.error("Error in pattern detection for case {}: {}", caseVertex.getCaseId(), e.getMessage());
        }
    }

    /**
     * Generate various types of alerts
     */
    private void generateAlerts(org.apache.camel.Exchange exchange) {
        CaseVertex caseVertex = exchange.getIn().getBody(CaseVertex.class);

        try {
            generateHighPriorityAlerts(caseVertex);
            log.info("Alert generation completed for case: {}", caseVertex.getCaseId());
        } catch (Exception e) {
            log.error("Error generating alerts for case {}: {}", caseVertex.getCaseId(), e.getMessage());
        }
    }

    private CaseVertex createCaseVertex(CaseReport caseReport) {
        CaseVertex caseVertex = new CaseVertex();
        caseVertex.setCaseId(caseReport.getCaseId());
        caseVertex.setCaseType(caseReport.getCaseType());
        caseVertex.setStatus(caseReport.getStatus());
        caseVertex.setReportedAt(caseReport.getReportedAt() != null ?
                caseReport.getReportedAt().toString() : null);
        caseVertex.setDescription(caseReport.getDescription());
        caseVertex.setReportingOfficer(caseReport.getReportingOfficer());
        return caseVertex;
    }

    private PersonVertex createPersonVertex(Person person) {
        PersonVertex personVertex = new PersonVertex();
        personVertex.setPersonId(person.getPersonId());
        personVertex.setFirstName(person.getFirstName());
        personVertex.setLastName(person.getLastName());
        personVertex.setContactNumber(person.getContactNumber());
        personVertex.setAddress(person.getAddress());
        return personVertex;
    }

    private LocationVertex createLocationVertex(Location location) {
        LocationVertex locationVertex = new LocationVertex();
        locationVertex.setAddress(location.getAddress());
        locationVertex.setCity(location.getCity());
        locationVertex.setState(location.getState());
        locationVertex.setCountry(location.getCountry());
        locationVertex.setPostalCode(location.getPostalCode());
        locationVertex.setDistrict(location.getDistrict());
        return locationVertex;
    }

    private void detectSimilarCases(CaseVertex caseVertex) {
        String query = String.format(
                "FOR case IN cases " +
                        "FILTER case.caseType == '%s' AND case.caseId != '%s' " +
                        "RETURN case",
                caseVertex.getCaseType(), caseVertex.getCaseId()
        );

        executeQuerySafely(query, "similar cases detection");
    }

    private void detectRecurringPersons(CaseVertex caseVertex) {
        String query = String.format(
                "FOR v, e, p IN 2..2 OUTBOUND 'cases/%s' case_person " +
                        "COLLECT person = v WITH COUNT INTO caseCount " +
                        "FILTER caseCount > 1 " +
                        "RETURN { person: person, caseCount: caseCount }",
                caseVertex.getCaseId()
        );

        executeQuerySafely(query, "recurring persons detection");
    }

    private void detectLocationHotspots() {
        String query =
                "FOR case IN cases " +
                        "FOR location IN 1..1 OUTBOUND case case_location " +
                        "COLLECT loc = location.district WITH COUNT INTO crimeCount " +
                        "FILTER crimeCount > 5 " +
                        "SORT crimeCount DESC " +
                        "RETURN { district: loc, crimeCount: crimeCount }";

        executeQuerySafely(query, "location hotspots detection");
    }

    private void generateHighPriorityAlerts(CaseVertex caseVertex) {
        String caseType = caseVertex.getCaseType();
        if ("ASSAULT".equals(caseType) || "ROBBERY".equals(caseType) || "HOMICIDE".equals(caseType)) {
            log.warn("HIGH PRIORITY ALERT: {} case reported - Case ID: {}",
                    caseType, caseVertex.getCaseId());
        }
    }

    private void executeQuerySafely(String query, String queryType) {
        try {
            graphDatabaseService.executeQuery(query);
            log.info("{} query executed successfully", queryType);
        } catch (Exception e) {
            log.error("Error executing {} query: {}", queryType, e.getMessage());
        }
    }
}