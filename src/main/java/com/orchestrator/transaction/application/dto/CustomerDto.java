package com.orchestrator.transaction.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    
    @JsonProperty("document_type")
    private String documentType;
    
    @JsonProperty("document_number")
    private String documentNumber;
    
    private String email;
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("last_name")
    private String lastName;
    
    @JsonProperty("country_calling_code")
    private String countryCallingCode;
    
    @JsonProperty("phone_number")
    private String phoneNumber;
    
    @JsonProperty("middle_name")
    private String middleName;
    
    @JsonProperty("second_last_name")
    private String secondLastName;
}
