package com.visor.school.events.user

import com.visor.school.events.EventBase
import java.time.Instant
import java.util.UUID

data class UserCreatedPayload(
    val userId: String,
    val email: String,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val role: String
)

class UserCreatedEvent(
    eventId: UUID = UUID.randomUUID(),
    aggregateId: String,
    occurredAt: Instant = Instant.now(),
    val payload: UserCreatedPayload
) : EventBase(
    eventId = eventId,
    eventType = "user.created",
    aggregateId = aggregateId,
    occurredAt = occurredAt
) {
    override fun getPayload(): Any = payload
}

