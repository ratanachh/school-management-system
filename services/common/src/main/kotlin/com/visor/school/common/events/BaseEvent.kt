package com.visor.school.common.events

import java.time.Instant
import java.util.UUID

/**
 * Base class for all domain events in the School Management System
 */
abstract class BaseEvent(
    open val eventId: UUID = UUID.randomUUID(),
    open val timestamp: Instant = Instant.now(),
    open val version: String = "1.0",
    open val eventType: String = ""
) {
    abstract fun getAggregateId(): UUID
    abstract fun getAggregateType(): String
}
