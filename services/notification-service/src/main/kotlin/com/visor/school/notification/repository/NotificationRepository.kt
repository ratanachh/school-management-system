package com.visor.school.notification.repository

import com.visor.school.notification.model.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface NotificationRepository : JpaRepository<Notification, UUID> {
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID): List<Notification>
    fun findByUserIdAndReadFalseOrderByCreatedAtDesc(userId: UUID): List<Notification>
}

