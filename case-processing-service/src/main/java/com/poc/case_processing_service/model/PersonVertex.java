package com.poc.case_processing_service.model;

import com.arangodb.entity.DocumentField;
import lombok.Data;

@Data
public class PersonVertex {
    @DocumentField(DocumentField.Type.KEY)
    private String key;

    private String personId;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String address;
}
