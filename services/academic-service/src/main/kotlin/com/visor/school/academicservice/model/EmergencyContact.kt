package com.visor.school.academicservice.model

import jakarta.persistence.Embeddable

/**
 * Emergency contact value object
 */
@Embeddable
data class EmergencyContact(
    val name: String,
    val relationship: String,
    val phoneNumber: String,
    val email: String? = null,
    val address: String? = null
)

