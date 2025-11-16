package com.visor.school.common.events

import java.time.Instant
import java.util.UUID

/**
 * Base class for all domain events in the School Management System
 */
abstract class BaseEvent(
    val eventId: UUID = UUID.randomUUID(),
    val timestamp: Instant = Instant.now(),
    val version: String = "1.0"
) {
    /**
     * Event type derived from the concrete class name
     */
    val eventType: String
        get() = this::class.simpleName ?: "UnknownEvent"

    abstract fun getAggregateId(): UUID
    abstract fun getAggregateType(): String
}

