package com.visor.school.events.user

import com.visor.school.events.EventBase
import java.time.Instant
import java.util.UUID

data class UserUpdatedPayload(
    val userId: String,
    val email: String?,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val status: String?
)

class UserUpdatedEvent(
    eventId: UUID = UUID.randomUUID(),
    aggregateId: String,
    occurredAt: Instant = Instant.now(),
    val payload: UserUpdatedPayload
) : EventBase(
    eventId = eventId,
    eventType = "user.updated",
    aggregateId = aggregateId,
    occurredAt = occurredAt
) {
    override fun getPayload(): Any = payload
}

