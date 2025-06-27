package com.poc.case_processing_service.model;

import com.arangodb.entity.DocumentField;
import lombok.Data;

@Data
public class LocationVertex {
    @DocumentField(DocumentField.Type.KEY)
    private String key;

    private String postalCode;
    private String city;
    private String state;
    private String country;
    private String address;
    private String district;
}
