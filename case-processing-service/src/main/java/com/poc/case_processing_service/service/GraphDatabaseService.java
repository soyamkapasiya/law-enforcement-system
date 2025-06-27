package com.poc.case_processing_service.service;

import com.arangodb.ArangoDatabase;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.model.DocumentCreateOptions;
import com.poc.case_processing_service.exception.GraphDatabaseException;
import com.poc.case_processing_service.model.CasePersonEdge;
import com.poc.case_processing_service.model.CaseVertex;
import com.poc.case_processing_service.model.LocationVertex;
import com.poc.case_processing_service.model.PersonVertex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Service
public class GraphDatabaseService {

    private final ArangoDatabase arangoDatabase;

    @Autowired
    public GraphDatabaseService(ArangoDatabase arangoDatabase) {
        this.arangoDatabase = arangoDatabase;
    }

    public String saveCaseToGraph(CaseVertex caseVertex) {
        validateNotNull(caseVertex, "CaseVertex cannot be null");

        try {
            DocumentCreateEntity<CaseVertex> result = arangoDatabase
                    .collection("cases")
                    .insertDocument(caseVertex, new DocumentCreateOptions().returnNew(true));

            String key = result.getKey();
            log.info("Case saved to graph with key: {}", key);
            return key;
        } catch (Exception e) {
            log.error("Error saving case to graph: {}", e.getMessage());
            throw new GraphDatabaseException("Failed to save case to graph", e);
        }
    }

    public String savePersonToGraph(PersonVertex personVertex) {
        validateNotNull(personVertex, "PersonVertex cannot be null");

        try {
            DocumentCreateEntity<PersonVertex> result = arangoDatabase
                    .collection("persons")
                    .insertDocument(personVertex, new DocumentCreateOptions().returnNew(true));

            String key = result.getKey();
            log.info("Person saved to graph with key: {}", key);
            return key;
        } catch (Exception e) {
            log.error("Error saving person to graph: {}", e.getMessage());
            throw new GraphDatabaseException("Failed to save person to graph", e);
        }
    }

    public void saveLocationToGraph(LocationVertex locationVertex) {
        validateNotNull(locationVertex, "LocationVertex cannot be null");

        try {
            DocumentCreateEntity<LocationVertex> result = arangoDatabase
                    .collection("locations")
                    .insertDocument(locationVertex, new DocumentCreateOptions().returnNew(true));

            String key = result.getKey();
            log.info("Location saved to graph with key: {}", key);
        } catch (Exception e) {
            log.error("Error saving location to graph: {}", e.getMessage());
            throw new GraphDatabaseException("Failed to save location to graph", e);
        }
    }

    public void createCasePersonRelationship(String caseKey, String personKey, String role) {
        validateNotEmpty(caseKey, "Case key cannot be empty");
        validateNotEmpty(personKey, "Person key cannot be empty");
        validateNotEmpty(role, "Role cannot be empty");

        try {
            CasePersonEdge edge = new CasePersonEdge();
            edge.setFrom("cases/" + caseKey);
            edge.setTo("persons/" + personKey);
            edge.setRole(role);
            edge.setRelationshipType("INVOLVED_IN");

            arangoDatabase
                    .collection("case_person")
                    .insertDocument(edge, new DocumentCreateOptions().returnNew(true));

            log.info("Case-Person relationship created: {} -> {} with role: {}", caseKey, personKey, role);
        } catch (Exception e) {
            log.error("Error creating case-person relationship: {}", e.getMessage());
            throw new GraphDatabaseException("Failed to create case-person relationship", e);
        }
    }

    public Optional<CaseVertex> getCaseByKey(String caseKey) {
        validateNotEmpty(caseKey, "Case key cannot be empty");

        try {
            CaseVertex caseVertex = arangoDatabase
                    .collection("cases")
                    .getDocument(caseKey, CaseVertex.class);

            return Optional.ofNullable(caseVertex);
        } catch (Exception e) {
            log.error("Error retrieving case with key: {}", caseKey, e);
            return Optional.empty();
        }
    }

    public Optional<PersonVertex> getPersonByKey(String personKey) {
        validateNotEmpty(personKey, "Person key cannot be empty");

        try {
            PersonVertex personVertex = arangoDatabase
                    .collection("persons")
                    .getDocument(personKey, PersonVertex.class);

            return Optional.ofNullable(personVertex);
        } catch (Exception e) {
            log.error("Error retrieving person with key: {}", personKey, e);
            return Optional.empty();
        }
    }

    public Optional<LocationVertex> getLocationByKey(String locationKey) {
        validateNotEmpty(locationKey, "Location key cannot be empty");

        try {
            LocationVertex locationVertex = arangoDatabase
                    .collection("locations")
                    .getDocument(locationKey, LocationVertex.class);

            return Optional.ofNullable(locationVertex);
        } catch (Exception e) {
            log.error("Error retrieving location with key: {}", locationKey, e);
            return Optional.empty();
        }
    }

    public boolean deleteCase(String caseKey) {
        validateNotEmpty(caseKey, "Case key cannot be empty");

        try {
            arangoDatabase.collection("cases").deleteDocument(caseKey);
            log.info("Case deleted with key: {}", caseKey);
            return true;
        } catch (Exception e) {
            log.error("Error deleting case with key: {}", caseKey, e);
            return false;
        }
    }

    public boolean deletePerson(String personKey) {
        validateNotEmpty(personKey, "Person key cannot be empty");

        try {
            arangoDatabase.collection("persons").deleteDocument(personKey);
            log.info("Person deleted with key: {}", personKey);
            return true;
        } catch (Exception e) {
            log.error("Error deleting person with key: {}", personKey, e);
            return false;
        }
    }

    public boolean deleteLocation(String locationKey) {
        validateNotEmpty(locationKey, "Location key cannot be empty");

        try {
            arangoDatabase.collection("locations").deleteDocument(locationKey);
            log.info("Location deleted with key: {}", locationKey);
            return true;
        } catch (Exception e) {
            log.error("Error deleting location with key: {}", locationKey, e);
            return false;
        }
    }

    public void executeQuery(String query) {
        validateNotEmpty(query, "Query cannot be empty");

        try {
            arangoDatabase.query(query, String.class);
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage());
            throw new GraphDatabaseException("Failed to execute query", e);
        }
    }

    private void validateNotNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateNotEmpty(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }
}