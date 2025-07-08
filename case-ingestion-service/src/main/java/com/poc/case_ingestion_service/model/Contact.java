package com.poc.case_ingestion_service.model;

import lombok.Data;

@Data
public class Contact {
    private String contactId;
    private String name;
    private String relationship;
    private String phoneNumber;
    private String email;
    private String address;
    private String contactType; // EMERGENCY, FAMILY, LEGAL, MEDICAL
}