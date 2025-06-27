package com.poc.case_ingestion_service.model;

import lombok.Data;

@Data
public class Location {
    private String address;
    private String postalCode;
    private String city;
    private String state;
    private String country;
    private String district;
}