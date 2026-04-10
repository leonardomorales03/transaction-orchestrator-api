package com.orchestrator.transaction.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private String documentType;
    private String documentNumber;
    private String email;
    private String firstName;
    private String lastName;
    private String countryCallingCode;
    private String phoneNumber;
    private String middleName;
    private String secondLastName;
}
