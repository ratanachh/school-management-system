package com.visor.school.events.user

import com.visor.school.events.EventBase
import java.time.Instant
import java.util.UUID

data class UserDeletedPayload(
    val userId: String,
    val deletedBy: String,
    val reason: String?
)

class UserDeletedEvent(
    eventId: UUID = UUID.randomUUID(),
    aggregateId: String,
    occurredAt: Instant = Instant.now(),
    val payload: UserDeletedPayload
) : EventBase(
    eventId = eventId,
    eventType = "user.deleted",
    aggregateId = aggregateId,
    occurredAt = occurredAt
) {
    override fun getPayload(): Any = payload
}

