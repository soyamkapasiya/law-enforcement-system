package com.poc.case_ingestion_service.model;

import lombok.Data;

import java.util.List;

@Data
public class Person {
    private String personId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String role; // SUSPECT, VICTIM, WITNESS, COMPLAINANT, ACCUSED
    private String contactNumber;
    private String alternateContact;
    private String email;
    private String address;
    private String age;
    private String gender;
    private String occupation;
    private String idType; // AADHAR, PAN, PASSPORT, LICENSE
    private String idNumber;
    private List<String> aliases;
    private String relationship; // To other persons in the case
}