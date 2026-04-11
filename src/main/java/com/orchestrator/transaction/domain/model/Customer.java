package com.orchestrator.transaction.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    // Campos obligatorios
    private String documentType;
    private String documentNumber;
    private String email;
    private String firstName;
    private String lastName;

    // Campos opcionales
    private String countryCallingCode;
    private String phoneNumber;
    private String middleName;
    private String secondLastName;
}
