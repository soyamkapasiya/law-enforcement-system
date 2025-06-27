package com.poc.case_ingestion_service.model;

import lombok.Data;

@Data
public class Person {
    private String personId;
    private String firstName;
    private String lastName;
    private String role; // SUSPECT, VICTIM, WITNESS
    private String contactNumber;
    private String address;
}