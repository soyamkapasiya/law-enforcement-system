# law-enforcement-system

# Law Enforcement Case Management System - Complete POC

## Business Use Case

**Scenario**: A city's police department needs to manage criminal cases, track relationships between suspects, victims, witnesses, and evidence. The system must handle real-time case updates, generate alerts for pattern recognition, and maintain a comprehensive graph database for relationship analysis.

### Key Requirements:
- Real-time case data ingestion from multiple sources (911 calls, patrol reports, detective updates)
- Data validation and enrichment
- Relationship mapping between entities (persons, locations, crimes, evidence)
- Pattern detection and alert generation
- Audit trail and compliance reporting

## Architecture Overview

```
[Data Sources] → [Spring Boot API] → [Apache Camel Processing] → [Kafka] → [Camel Consumer] → [ArangoDB]
                                                                      ↓
                                                              [Alert Service]
```

## Project Structure

```
law-enforcement-system/
├── case-ingestion-service/
│   ├── src/main/java/com/police/ingestion/
│   ├── pom.xml
│   └── application.yml
├── case-processing-service/
│   ├── src/main/java/com/police/processing/
│   ├── pom.xml
│   └── application.yml
├── docker-compose.yml
└── README.md
```

## Combined Benefit:
```

| Tech           | Role                            | Why It's Used                                                        |
| -------------- | ------------------------------- | -------------------------------------------------------------------- |
| Spring Boot    | REST API layer (data ingestion) | Easy microservice setup, integrates well with Camel, Kafka, ArangoDB |
| Apache Camel   | Routing and processing          | Clean and maintainable integration between components                |
| Apache Kafka   | Asynchronous messaging bus      | Reliable decoupling, scalability, event reprocessing                 |
| ArangoDB       | Persistent graph data store     | Models case relationships naturally, powerful queries                |
| Docker Compose | DevOps & infra setup            | Easy to run Kafka, ArangoDB, Zookeeper locally                       |

```