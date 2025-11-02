package com.visor.school.persistence

import com.visor.school.common.exception.ResourceNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import java.util.*

/**
 * Base repository interface
 */
@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, UUID> {
    
    /**
     * Find entity by ID or throw exception
     */
    fun findByIdOrThrow(id: UUID): T {
        return findById(id).orElseThrow {
            ResourceNotFoundException(
                "Resource with id $id not found",
                this.javaClass.simpleName
            )
        }
    }

}

