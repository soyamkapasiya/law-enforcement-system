package com.poc.case_processing_service.model;

import com.arangodb.serde.jackson.Key;
import com.arangodb.springframework.annotation.Document;
import lombok.Data;

@Data
public class PersonVertex {

    @Key
    private String key;

    private String personId;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String address;
}
