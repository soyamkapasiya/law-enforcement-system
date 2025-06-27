package com.poc.case_processing_service.service;

import com.arangodb.ArangoDatabase;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import com.poc.case_processing_service.exception.GraphDatabaseException;
import com.poc.case_processing_service.model.*;
import com.poc.case_processing_service.repository.CasePersonEdgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphDatabaseService {

    private final ArangoDatabase arangoDatabase;
    private final CasePersonEdgeRepository casePersonEdgeRepository;

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

    public String saveEvidenceToGraph(EvidenceVertex evidenceVertex) {
        validateNotNull(evidenceVertex, "EvidenceVertex cannot be null");

        try {
            DocumentCreateEntity<EvidenceVertex> result = arangoDatabase
                    .collection("evidence")
                    .insertDocument(evidenceVertex, new DocumentCreateOptions().returnNew(true));

            String key = result.getKey();
            log.info("Evidence saved to graph with key: {}", key);
            return key;
        } catch (Exception e) {
            log.error("Error saving evidence to graph: {}", e.getMessage());
            throw new GraphDatabaseException("Failed to save evidence to graph", e);
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

    public String saveLocationToGraph(LocationVertex locationVertex) {
        validateNotNull(locationVertex, "LocationVertex cannot be null");

        try {
            DocumentCreateEntity<LocationVertex> result = arangoDatabase
                    .collection("locations")
                    .insertDocument(locationVertex, new DocumentCreateOptions().returnNew(true));

            String key = result.getKey();
            log.info("Location saved to graph with key: {}", key);
            return key;
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
            tryRepositoryApproach(caseKey, personKey, role);
        } catch (Exception e) {
            log.warn("Repository approach failed, trying AQL query approach: {}", e.getMessage());
            tryAQLApproach(caseKey, personKey, role);
        }
    }

    // NEW: Create case-evidence relationship
    public void createCaseEvidenceRelationship(String caseKey, String evidenceKey) {
        validateNotEmpty(caseKey, "Case key cannot be empty");
        validateNotEmpty(evidenceKey, "Evidence key cannot be empty");

        String aqlQuery = String.format(
                "INSERT { _from: 'cases/%s', _to: 'evidence/%s', relationshipType: 'HAS_EVIDENCE' } INTO case_evidence",
                caseKey, evidenceKey
        );

        try {
            executeQuery(aqlQuery);
            log.info("Case-Evidence relationship created: {} -> {}", caseKey, evidenceKey);
        } catch (Exception e) {
            log.error("Failed to create case-evidence relationship: {}", e.getMessage());
            throw new GraphDatabaseException("Failed to create case-evidence relationship", e);
        }
    }

    // NEW: Create case-location relationship
    public void createCaseLocationRelationship(String caseKey, String locationKey) {
        validateNotEmpty(caseKey, "Case key cannot be empty");
        validateNotEmpty(locationKey, "Location key cannot be empty");

        String aqlQuery = String.format(
                "INSERT { _from: 'cases/%s', _to: 'locations/%s', relationshipType: 'OCCURRED_AT' } INTO case_location",
                caseKey, locationKey
        );

        try {
            executeQuery(aqlQuery);
            log.info("Case-Location relationship created: {} -> {}", caseKey, locationKey);
        } catch (Exception e) {
            log.error("Failed to create case-location relationship: {}", e.getMessage());
            throw new GraphDatabaseException("Failed to create case-location relationship", e);
        }
    }

    private void tryRepositoryApproach(String caseKey, String personKey, String role) {
        CasePersonEdge edge = new CasePersonEdge();
        edge.setFrom("cases/" + caseKey);
        edge.setTo("persons/" + personKey);
        edge.setRole(role);
        edge.setRelationshipType("INVOLVED_IN");

        log.info("Creating edge using repository with from={} to={} role={}",
                edge.getFrom(), edge.getTo(), edge.getRole());

        CasePersonEdge savedEdge = casePersonEdgeRepository.save(edge);
        log.info("Case-Person relationship created via repository: {} -> {} with role: {}",
                caseKey, personKey, role);
    }

    private void tryAQLApproach(String caseKey, String personKey, String role) {
        String aqlQuery = String.format(
                "INSERT { _from: 'cases/%s', _to: 'persons/%s', role: '%s', relationshipType: 'INVOLVED_IN' } INTO case_person",
                caseKey, personKey, role
        );

        try {
            executeQuery(aqlQuery);
            log.info("Case-Person relationship created via AQL: {} -> {} with role: {}", caseKey, personKey, role);
        } catch (Exception e) {
            log.error("Failed to create relationship via AQL: {}", e.getMessage());
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

    public Optional<EvidenceVertex> getEvidenceByKey(String evidenceKey) {
        validateNotEmpty(evidenceKey, "Evidence key cannot be empty");

        try {
            EvidenceVertex evidenceVertex = arangoDatabase
                    .collection("evidence")
                    .getDocument(evidenceKey, EvidenceVertex.class);

            return Optional.ofNullable(evidenceVertex);
        } catch (Exception e) {
            log.error("Error retrieving evidence with key: {}", evidenceKey, e);
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

    public boolean deleteEvidence(String evidenceKey) {
        validateNotEmpty(evidenceKey, "Evidence key cannot be empty");

        try {
            arangoDatabase.collection("evidence").deleteDocument(evidenceKey);
            log.info("Evidence deleted with key: {}", evidenceKey);
            return true;
        } catch (Exception e) {
            log.error("Error deleting evidence with key: {}", evidenceKey, e);
            return false;
        }
    }

    public List<Map<String, Object>> executeQuery(String query) {
        validateNotEmpty(query, "Query cannot be empty");

        try {
            log.debug("Executing AQL query: {}", query);
            ArangoCursor<BaseDocument> cursor = arangoDatabase.query(query, BaseDocument.class);

            List<Map<String, Object>> results = new ArrayList<>();
            cursor.forEach(doc -> results.add(doc.getProperties()));

            log.debug("Query executed successfully, returned {} results", results.size());
            return results;
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage());
            throw new GraphDatabaseException("Failed to execute query", e);
        }
    }

    public <T> List<T> executeQuery(String query, Class<T> type) {
        validateNotEmpty(query, "Query cannot be empty");
        validateNotNull(type, "Type cannot be null");

        try {
            log.debug("Executing AQL query with type {}: {}", type.getSimpleName(), query);
            ArangoCursor<T> cursor = arangoDatabase.query(query, type);

            List<T> results = new ArrayList<>();
            cursor.forEach(results::add);

            log.debug("Query executed successfully, returned {} results", results.size());
            return results;
        } catch (Exception e) {
            log.error("Error executing query with type {}: {}", type.getSimpleName(), e.getMessage());
            throw new GraphDatabaseException("Failed to execute query", e);
        }
    }

    public long executeCountQuery(String query) {
        validateNotEmpty(query, "Query cannot be empty");

        try {
            log.debug("Executing count query: {}", query);
            ArangoCursor<Long> cursor = arangoDatabase.query(query, Long.class);

            return cursor.hasNext() ? cursor.next() : 0L;
        } catch (Exception e) {
            log.error("Error executing count query: {}", e.getMessage());
            throw new GraphDatabaseException("Failed to execute count query", e);
        }
    }

    // FIXED: Collection management methods
    public boolean collectionExists(String collectionName) {
        try {
            return !arangoDatabase.collection(collectionName).exists();
        } catch (Exception e) {
            log.error("Error checking if collection exists: {}", collectionName, e);
            return true;
        }
    }

    public void createCollectionIfNotExists(String collectionName) {
        try {
            if (collectionExists(collectionName)) {
                arangoDatabase.createCollection(collectionName);
                log.info("Created collection: {}", collectionName);
            }
        } catch (Exception e) {
            log.error("Error creating collection: {}", collectionName, e);
            throw new GraphDatabaseException("Failed to create collection: " + collectionName, e);
        }
    }

    public void createEdgeCollectionIfNotExists(String collectionName) {
        try {
            if (collectionExists(collectionName)) {
                arangoDatabase.createCollection(collectionName,
                        new com.arangodb.model.CollectionCreateOptions().type(com.arangodb.entity.CollectionType.EDGES));
                log.info("Created edge collection: {}", collectionName);
            }
        } catch (Exception e) {
            log.error("Error creating edge collection: {}", collectionName, e);
            throw new GraphDatabaseException("Failed to create edge collection: " + collectionName, e);
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

    public List<Map<String, Object>> executeQuerySafely(String query, String... requiredCollections) {
        for (String collection : requiredCollections) {
            if (collectionExists(collection)) {
                log.warn("Required collection '{}' does not exist for query", collection);
                throw new GraphDatabaseException("Required collection '" + collection + "' does not exist");
            }
        }

        return executeQuery(query);
    }
}