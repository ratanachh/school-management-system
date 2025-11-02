package com.visor.school.user.domain.model

import com.visor.school.common.constant.Constants
import com.visor.school.persistence.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_email", columnList = "email", unique = true),
        Index(name = "idx_keycloak_id", columnList = "keycloak_id", unique = true)
    ]
)
class User : BaseEntity() {

    @Column(name = "keycloak_id")
    var keycloakId: String? = null

    @Column(name = "email", nullable = false, unique = true)
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    @field:Size(max = Constants.MAX_EMAIL_LENGTH)
    var email: String = ""

    @Column(name = "username")
    @field:Size(min = Constants.MIN_USERNAME_LENGTH, max = Constants.MAX_USERNAME_LENGTH)
    var username: String? = null

    @Column(name = "first_name")
    @field:Size(max = Constants.MAX_NAME_LENGTH)
    var firstName: String? = null

    @Column(name = "last_name")
    @field:Size(max = Constants.MAX_NAME_LENGTH)
    var lastName: String? = null

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var status: UserStatus = UserStatus.ACTIVE

    @Column(name = "email_verified")
    var emailVerified: Boolean = false

    @Column(name = "roles", columnDefinition = "TEXT")
    var roles: String? = null

    fun getFullName(): String {
        return listOfNotNull(firstName, lastName).joinToString(" ").ifEmpty { email }
    }

    fun hasRole(role: String): Boolean {
        return roles?.split(",")?.contains(role) ?: false
    }
}

enum class UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    DELETED
}

