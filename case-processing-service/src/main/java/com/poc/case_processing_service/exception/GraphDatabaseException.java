package com.poc.case_processing_service.exception;

public class GraphDatabaseException extends RuntimeException {
    public GraphDatabaseException(String message) {
        super(message);
    }

    public GraphDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}