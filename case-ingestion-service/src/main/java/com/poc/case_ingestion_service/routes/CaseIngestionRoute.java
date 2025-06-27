package com.poc.case_ingestion_service.routes;

import com.poc.case_ingestion_service.model.CaseReport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class CaseIngestionRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:processCase")
                .routeId("case-ingestion-route")
                .log("Processing case: ${body.caseId}")
                .to("direct:validateCase")
                .to("direct:enrichCase")
                .to("direct:sendToKafka");

        from("direct:validateCase")
                .routeId("case-validation-route")
                .log("Validating case: ${body.caseId}")
                .choice()
                .when(simple("${body.caseType} == null"))
                .throwException(new IllegalArgumentException("Case type is required"))
                .when(simple("${body.status} == null"))
                .throwException(new IllegalArgumentException("Case status is required"))
                .otherwise()
                .log("Case validation passed");

        from("direct:enrichCase")
                .routeId("case-enrichment-route")
                .log("Enriching case: ${body.caseId}")
                .process(exchange -> {
                    CaseReport caseReport = exchange.getIn().getBody(CaseReport.class);

                    if (caseReport.getReportedAt() == null) {
                        caseReport.setReportedAt(java.time.LocalDateTime.now());
                    }

                    if (caseReport.getCaseId() == null || caseReport.getCaseId().isEmpty()) {
                        caseReport.setCaseId("CASE-" + System.currentTimeMillis());
                    }

                    exchange.getIn().setBody(caseReport);
                });

        from("direct:sendToKafka")
                .routeId("kafka-producer-route")
                .marshal().json(JsonLibrary.Jackson)
                .log("Sending to Kafka: ${body}")
                .to("kafka:case-events?brokers=localhost:9092")
                .log("Case sent to Kafka successfully");
    }
}