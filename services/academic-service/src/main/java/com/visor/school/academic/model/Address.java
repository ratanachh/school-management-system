package com.visor.school.academic.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Address value object
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country = "Cambodia";

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(street);
        if (state != null) {
            sb.append(", ").append(state);
        }
        sb.append(", ").append(city);
        sb.append(" ").append(postalCode);
        if (!"Cambodia".equals(country)) {
            sb.append(", ").append(country);
        }
        return sb.toString();
    }
}
