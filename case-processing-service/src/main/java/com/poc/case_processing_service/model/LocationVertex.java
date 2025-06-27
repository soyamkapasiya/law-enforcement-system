package com.poc.case_processing_service.model;


import com.arangodb.serde.jackson.Key;
import lombok.Data;

@Data
public class LocationVertex {

    @Key
    private String key;

    private String postalCode;
    private String city;
    private String state;
    private String country;
    private String address;
    private String district;
}
