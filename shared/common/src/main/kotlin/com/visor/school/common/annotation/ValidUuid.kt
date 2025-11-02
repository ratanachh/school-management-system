package com.visor.school.common.annotation

import com.visor.school.common.util.ValidationUtil
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidUuidValidator::class])
@MustBeDocumented
annotation class ValidUuid(
    val message: String = "Invalid UUID format",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ValidUuidValidator : ConstraintValidator<ValidUuid, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrBlank()) return true // Use @NotNull for required validation
        return ValidationUtil.isValidUuid(value)
    }
}

