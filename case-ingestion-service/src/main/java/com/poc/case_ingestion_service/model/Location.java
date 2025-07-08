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
    private String precinct;
    private Double latitude;
    private Double longitude;
    private String landmark;
    private String locationType; // RESIDENTIAL, COMMERCIAL, PUBLIC, HIGHWAY
}