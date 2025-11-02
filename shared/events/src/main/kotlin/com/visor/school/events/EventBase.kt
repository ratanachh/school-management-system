package com.visor.school.events

import java.time.Instant
import java.util.UUID

/**
 * Base class for all domain events
 */
abstract class EventBase(
    val eventId: UUID = UUID.randomUUID(),
    val eventType: String,
    val aggregateId: String,
    val occurredAt: Instant = Instant.now(),
    val version: String = "1.0"
) {
    abstract fun getPayload(): Any
}

