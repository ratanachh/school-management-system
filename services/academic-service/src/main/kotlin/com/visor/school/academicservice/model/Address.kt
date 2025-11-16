package com.visor.school.academicservice.model

import jakarta.persistence.Embeddable

/**
 * Address value object
 */
@Embeddable
data class Address(
    val street: String,
    val city: String,
    val state: String? = null,
    val postalCode: String,
    val country: String = "Cambodia"
) {
    fun getFullAddress(): String {
        return buildString {
            append(street)
            if (state != null) {
                append(", $state")
            }
            append(", $city")
            append(" $postalCode")
            if (country != "Cambodia") {
                append(", $country")
            }
        }
    }
}

