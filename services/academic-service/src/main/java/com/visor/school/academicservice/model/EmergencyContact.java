package com.visor.school.academicservice.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Emergency contact value object
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContact {
    private String name;
    private String relationship;
    private String phoneNumber;
    private String email;
    private String address;
}
