package com.poc.case_processing_service.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.poc.case_processing_service.model.CasePersonEdge;
import org.springframework.stereotype.Repository;

@Repository
public interface CasePersonEdgeRepository extends ArangoRepository<CasePersonEdge, String> {
}
